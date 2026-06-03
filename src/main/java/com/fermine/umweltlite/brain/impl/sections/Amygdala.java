package com.fermine.umweltlite.brain.impl.sections;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.world.entity.LivingEntity;

public class Amygdala implements IBrainComponent {

    private final BrainStem brainStem; // Cross-talk: Primal emotion requires physiological data

    // Core Floaters
    private float panicFloater = 0.0f;      // 0.0 (Zen) to 1.0 (Absolute Dread)
    private float aggressionFloater = 0.0f;  // 0.0 (Docile) to 1.0 (Enraged)

    public Amygdala(BrainStem brainStem) {
        this.brainStem = brainStem;
    }

    @Override
    public void tick(LivingEntity host) {
        // 1. Natural Decay: Fear and anger cool down slowly over time if unprovoked
        this.panicFloater = Math.max(0.0f, this.panicFloater - 0.005f);
        this.aggressionFloater = Math.max(0.0f, this.aggressionFloater - 0.003f);

        // 2. Somatic Cross-talk: If the brain stem is hyper-exerted or suffocating, fuel the panic
        if (brainStem.isSuffocating()) {
            triggerShock(0.05f); // Constant rising panic while drowning/suffocating
        }

        if (brainStem.getHeartRate() > 130.0f) {
            // High heart rate naturally keeps the organism on edge, slowing panic decay
            this.panicFloater = Math.min(1.0f, this.panicFloater + 0.001f);
        }
    }

    @Override
    public void reset() {
        this.panicFloater = 0.0f;
        this.aggressionFloater = 0.0f;
    }

    /**
     * Instantly spikes fear/panic metrics. Called by external sensory systems
     * (like the Parietal Lobe registering pain or the Occipital Lobe seeing a threat).
     */
    public void triggerShock(float intensity) {
        this.panicFloater = Math.min(1.0f, this.panicFloater + intensity);
        // A panicked mind can trigger a sudden burst of energy back into the brainstem
        this.brainStem.exertEnergy(intensity * 0.2f);
    }

    public void provokeAggression(float amount) {
        this.aggressionFloater = Math.min(1.0f, this.aggressionFloater + amount);
    }

    // Getters
    public float getPanicFloater() { return this.panicFloater; }
    public float getAggressionFloater() { return this.aggressionFloater; }
}