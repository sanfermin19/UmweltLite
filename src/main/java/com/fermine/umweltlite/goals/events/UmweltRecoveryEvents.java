package com.fermine.umweltlite.goals.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltRecoveryEvents {

    /**
     * When a mob naturally heals (e.g., from eating grass, resting, or potion effects),
     * its biological recovery triggers an emotional recovery.
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof IUmweltEntity umweltMob) {
            UmweltEngine engine = umweltMob.getUmweltEngine();
            if (engine == null) return;

            // Healing soothes the mob: Increases Valence (happiness), Decreases Arousal (panic)
            // The amount of recovery scales slightly with the heal amount
            float healAmount = event.getAmount();
            float valenceBoost = Math.min(0.3f, healAmount * 0.05f);
            float arousalDrop = Math.min(0.4f, healAmount * 0.08f);

            float currentValence = engine.getEmotionalEngine().getValence();
            float currentArousal = engine.getEmotionalEngine().getArousal();

            EmotionAPI.setValence(engine, Math.min(1.0f, currentValence + valenceBoost));
            EmotionAPI.setArousal(engine, Math.max(0.0f, currentArousal - arousalDrop));

            // Re-energize the mob
            EmotionAPI.setEnergy(engine, Math.min(1.0f, engine.getEmotionalEngine().getEnergy() + 0.1f));
        }
    }

    /**
     * Allows a player to slowly build a social attachment and soothe a mob by interacting with it
     * (e.g., feeding it, or right-clicking it with an empty hand to "pet" it).
     */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;

        if (event.getTarget() instanceof IUmweltEntity umweltMob) {
            UmweltEngine engine = umweltMob.getUmweltEngine();
            Player player = event.getEntity();
            if (engine == null) return;

            // Only highly social mobs process this interaction deeply
            if (PersonalityAPI.isSocial(engine)) {

                // Boost valence because interaction is generally positive for social creatures
                float currentValence = engine.getEmotionalEngine().getValence();
                EmotionAPI.setValence(engine, Math.min(1.0f, currentValence + 0.1f));

                // Modify the social attachment map directly using the new EmotionAPI method
                // We slowly increase Valence and Bond with the player, while dropping Arousal (fear)
                EmotionAPI.modifySocialAttachment(
                        engine,
                        player.getUUID(),
                        0.15f,  // vDelta: Increase trust
                        -0.1f,  // iDelta (arousal): Decrease fear/panic around this player
                        0.05f   // bDelta: Slowly build the long-term bond
                );

                // If they interact enough, they might technically become "mates" / deeply bonded
                if (EmotionAPI.isMate(engine, player.getUUID())) {
                    // Trigger a massive energy boost or special behavior hook here
                    EmotionAPI.setEnergy(engine, 1.0f);
                }
            }
        }
    }
}