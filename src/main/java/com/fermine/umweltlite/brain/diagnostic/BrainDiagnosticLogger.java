package com.fermine.umweltlite.brain.diagnostic;

import com.fermine.umweltlite.brain.capability.UmweltAttachments;
import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Clean, lightweight replacement for the old UI diagnostics.
 * Directly extracts server-authoritative engine data and prints a readable snapshot to the player's chat.
 */
public class BrainDiagnosticLogger {

    /**
     * Reads the psychological and emotional profile of a target mob and mirrors it directly to the player.
     * Run this exclusively on the server thread context (e.g., from an item interaction or command).
     *
     * @param player The player requesting the diagnostic read-out.
     * @param targetMob The targeted organism entity.
     */
    public static void printDiagnosticsToChat(Player player, Mob targetMob) {
        if (player.level().isClientSide()) {
            return;
        }

        // Send a clean section header separator
        player.sendSystemMessage(Component.literal("========================================").withStyle(ChatFormatting.DARK_GRAY));

        // Entity Name Header
        Component entityName = targetMob.getType().getDescription();
        player.sendSystemMessage(Component.literal("UMWELT COGNITIVE DIAGNOSTIC: ")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.literal(entityName.getString()).withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE)));

        // Verify Data Attachment Presence
        if (!targetMob.hasData(UmweltAttachments.BRAIN_PIPELINE)) {
            player.sendSystemMessage(Component.literal("Core Status: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("OFFLINE (No pipeline attachment found)").withStyle(ChatFormatting.RED)));
            player.sendSystemMessage(Component.literal("========================================").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        IUmweltBrainPipeline pipeline = targetMob.getData(UmweltAttachments.BRAIN_PIPELINE);

        // 1. Structural Identity Mappings
        player.sendSystemMessage(Component.literal("Pipeline ID: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(pipeline.getBrainTypeId().toString()).withStyle(ChatFormatting.DARK_AQUA)));
        player.sendSystemMessage(Component.literal("Active Stage: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(pipeline.getActiveStageName().toUpperCase()).withStyle(ChatFormatting.YELLOW)));

        player.sendSystemMessage(Component.literal("--- Affect Engine (Circumplex) ---").withStyle(ChatFormatting.YELLOW));

        // 2. Emotional Metrics (Valence and Arousal formatting)
        float valence = pipeline.getValence();
        float arousal = pipeline.getArousal();

        ChatFormatting valenceColor = valence >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
        ChatFormatting arousalColor = arousal > 0.4f ? ChatFormatting.GOLD : (arousal < -0.4f ? ChatFormatting.BLUE : ChatFormatting.GRAY);

        player.sendSystemMessage(Component.literal(String.format("  • Valence (Atmosphere): %.2f ", valence)).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(valence >= 0 ? "[PLEASANT]" : "[DISTRESSED]").withStyle(valenceColor)));
        player.sendSystemMessage(Component.literal(String.format("  • Arousal (Alertness):  %.2f ", arousal)).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(arousal > 0.4f ? "[HYPER-ACTIVE]" : (arousal < -0.4f ? "[TORPOR]" : "[CALM]")).withStyle(arousalColor)));

        player.sendSystemMessage(Component.literal("--- Driven Registers (Triad) ---").withStyle(ChatFormatting.YELLOW));

        // 3. Cognitive Triad Domain Registers
        float survival = pipeline.getSurvival();
        float selfInterest = pipeline.getSelfInterest();
        float analytical = pipeline.getAnalytical();

        player.sendSystemMessage(Component.literal(String.format("  • Survival (Reactive):   %.2f", survival))
                .withStyle(survival > 0.6f ? ChatFormatting.RED : ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(String.format("  • Self-Interest (Bio):   %.2f", selfInterest))
                .withStyle(selfInterest > 0.6f ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(String.format("  • Analytical (Patterns):  %.2f", analytical))
                .withStyle(analytical > 0.6f ? ChatFormatting.GREEN : ChatFormatting.GRAY));

        player.sendSystemMessage(Component.literal("--- Genetic Traits (Personality Profiles) ---").withStyle(ChatFormatting.YELLOW));

        // 4. Persistent Personality Trait Dump
        String[] traits = {"bravery", "anxiety", "empathy", "focus", "analytical", "playfulness"};
        for (String trait : traits) {
            float val = pipeline.getPersonalityTrait(trait);
            player.sendSystemMessage(Component.literal(String.format("  • %-12s : %.2f", trait, val)).withStyle(ChatFormatting.DARK_GREEN));
        }

        player.sendSystemMessage(Component.literal("========================================").withStyle(ChatFormatting.DARK_GRAY));
    }
}