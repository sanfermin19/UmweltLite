package com.fermine.umweltlite.brain.impl.lobes;

import com.fermine.umweltlite.brain.impl.sections.Amygdala;
import com.fermine.umweltlite.brain.inter.lobes.IParietalLobe;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

public class ParietalLobe implements IParietalLobe {

    private final Amygdala amygdala; // Cross-talk: Pain immediately spikes fear

    // Core Sensory Floaters
    private float painIndex = 0.0f;       // 0.0 (Comfortable) to 1.0 (Agonizing Pain)
    private float thermalComfort = 0.5f;  // 0.0 (Freezing), 0.5 (Neutral), 1.0 (Scorching)
    private BlockPos lastKnownPosition = BlockPos.ZERO;

    public ParietalLobe(Amygdala amygdala) {
        this.amygdala = amygdala;
    }

    @Override
    public void tick(LivingEntity host) {
        // 1. Natural Pain Recovery: Pain fades if no new damage is taken
        this.painIndex = Math.max(0.0f, this.painIndex - 0.01f);

        // 2. Track Spatial Coordinates
        this.lastKnownPosition = host.blockPosition();

        // 3. Thermal & Somatosensory Sampling
        //  the environment directly surrounding the host's body
        BlockPos bodyPos = host.blockPosition();
        boolean nearFireSource = host.level().getBlockState(bodyPos).is(Blocks.LAVA) ||
                host.level().getBlockState(bodyPos.above()).is(Blocks.FIRE);
        boolean inPowderSnow = host.isInPowderSnow;

        if (nearFireSource || host.isOnFire()) {
            this.thermalComfort = Math.min(1.0f, this.thermalComfort + 0.05f);
        } else if (inPowderSnow) {
            this.thermalComfort = Math.max(0.0f, this.thermalComfort - 0.05f);
        } else {
            // Return organically to thermal baseline (0.5)
            this.thermalComfort += (0.5f - this.thermalComfort) * 0.02f;
        }

        // 4. Somatic Stress Feedback: Extreme heat or cold subtly bleeds into panic
        if (this.thermalComfort > 0.85f || this.thermalComfort < 0.15f) {
            this.amygdala.triggerShock(0.002f);
        }
    }

    /**
     * Injected via Forge/NeoForge living damage events.
     * Maps raw damage sources into a localized sensory impact.
     */
    public void reflectPhysicalDamage(DamageSource source, float amount) {
        // Calculate somatic impact based on damage intensity relative to a base scale
        float calculatedImpact = Math.min(1.0f, amount / 10.0f);
        this.painIndex = Math.min(1.0f, this.painIndex + calculatedImpact);

        // Cross-talk: Pass physical shock directly up to the Amygdala
        float shockWeight = calculatedImpact;
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            shockWeight *= 1.5f; // Internal or magical pain triggers deeper panic
        }
        this.amygdala.triggerShock(shockWeight);
    }

    @Override
    public void reset() {
        this.painIndex = 0.0f;
        this.thermalComfort = 0.5f;
        this.lastKnownPosition = BlockPos.ZERO;
    }

    // Getters
    public float getPainIndex() { return this.painIndex; }
    public float getThermalComfort() { return this.thermalComfort; }
    public BlockPos getLastKnownPosition() { return this.lastKnownPosition; }
}