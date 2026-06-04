package com.fermine.umweltlite.brain.diagnostic;

import com.fermine.umweltlite.impl.entity.UmweltCow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class BrainDiagnosticScreen extends Screen {
    private final Mob targetMob;

    public BrainDiagnosticScreen(Mob targetMob) {
        super(Component.literal("Umwelt Cognitive Diagnostic Panel"));
        this.targetMob = targetMob;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int startX = this.width / 2 - 150;
        int startY = 40;

        // Title Header
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        Component localizedEntityName = Component.translatable(targetMob.getType().getDescriptionId());
        guiGraphics.drawString(this.font, "Entity Target: " + localizedEntityName.getString(), startX, startY, 0xAAAAAA);
        guiGraphics.drawString(this.font, "Cognitive Core Status: ACTIVE", startX, startY + 15, 0x55FF55);

        guiGraphics.drawString(this.font, "--- Brain Metrics ---", startX, startY + 40, 0xFFFF55);

        // FIX: Read safe network parameters instead of using the raw server capability layer directly
        if (targetMob instanceof UmweltCow cow) {
            float livePanic = cow.getSyncedPanic();
            float liveExhaustion = cow.getSyncedExhaustion();
            String activeAction = cow.getSyncedActiveAction();

            // Format metrics smoothly to 2 decimal points
            guiGraphics.drawString(this.font, String.format("Amygdala Distress Level: %.2f", livePanic), startX, startY + 55, livePanic > 0.5f ? 0xFF5555 : 0xAAAAAA);
            guiGraphics.drawString(this.font, String.format("Metabolic Exhaustion Delta: %.2f", liveExhaustion), startX, startY + 70, 0x55FFFF);

            // --- THE EXECUTIVE SNAPSHOT LINE ---
            guiGraphics.drawString(this.font, "--- Executive Strategy ---", startX, startY + 95, 0xFFFF55);
            guiGraphics.drawString(this.font, "Dominant Pool Action: " + (activeAction != null ? activeAction.toUpperCase() : "IDLE"), startX, startY + 110, 0xFFAA00);
        } else {
            // Friendly fallback if you open the panel on an entity that hasn't been migrated to your custom core yet
            guiGraphics.drawString(this.font, "Unable to read neural link data.", startX, startY + 55, 0xFF5555);
            guiGraphics.drawString(this.font, "Target is not a registered Umwelt organism.", startX, startY + 70, 0xAAAAAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0x60101010, 0x60101010);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}