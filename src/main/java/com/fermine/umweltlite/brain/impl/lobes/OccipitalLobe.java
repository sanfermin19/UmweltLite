package com.fermine.umweltlite.brain.impl.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class OccipitalLobe implements IBrainComponent {

    // Visual tracking states
    private UUID focusedTargetUUID = null;
    private float visualAcuity = 1.0f;   // 1.0 (Perfect Vision) to 0.0 (Blind)
    private int trackingTicks = 0;

    @Override
    public void tick(LivingEntity host) {
        // 1. Process Vision Degradation
        if (host.hasEffect(MobEffects.BLINDNESS)) {
            this.visualAcuity = 0.0f;
        } else {
            // Check light levels at eye position to determine acuity
            float localLight = host.level().getMaxLocalRawBrightness(host.blockPosition().above());
            // Map 0-15 light level to a 0.4 - 1.0 acuity range (mobs can see in the dark, but worse)
            this.visualAcuity = 0.4f + (localLight / 15.0f) * 0.6f;
        }

        // 2. Track focus decay
        if (this.focusedTargetUUID != null) {
            this.trackingTicks++;
            if (this.trackingTicks > 100 || this.visualAcuity < 0.2f) {
                // Lose visual focus if tracking too long without a refresh or if blinded
                clearFocus();
            }
        }
    }

    /**
     * Invoked by your OpticalSense raycasting engine.
     * Signals that the occipital lobe has successfully resolved an entity in its field of view.
     */
    public void registerEntityInVisualField(Entity entity) {
        if (this.visualAcuity < 0.1f) return; // Can't resolve targets if blind

        // Focus lock mechanism
        if (this.focusedTargetUUID == null || !this.focusedTargetUUID.equals(entity.getUUID())) {
            this.focusedTargetUUID = entity.getUUID();
            this.trackingTicks = 0;
        }
    }

    public void clearFocus() {
        this.focusedTargetUUID = null;
        this.trackingTicks = 0;
    }

    @Override
    public void reset() {
        this.focusedTargetUUID = null;
        this.visualAcuity = 1.0f;
        this.trackingTicks = 0;
    }

    // Getters
    public Optional<UUID> getFocusedTargetUUID() { return Optional.ofNullable(this.focusedTargetUUID); }
    public float getVisualAcuity() { return this.visualAcuity; }
    public boolean isTargetInFocus() { return this.focusedTargetUUID != null; }
}