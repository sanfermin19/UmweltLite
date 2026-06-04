package com.fermine.umweltlite.brain.impl.sections;

import com.fermine.umweltlite.brain.inter.sections.IAmygdala; // FIX: Import the proper interface!
import net.minecraft.world.entity.LivingEntity;

// FIX: Swap from IBrainComponent to IAmygdala
public class Amygdala implements IAmygdala {

    private final BrainStem brainStem;

    // Core Floaters
    private float panicFloater = 0.0f;
    private float aggressionFloater = 0.0f;

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
            triggerShock(0.05f);
        }

        if (brainStem.getHeartRate() > 130.0f) {
            this.panicFloater = Math.min(1.0f, this.panicFloater + 0.001f);
        }
    }

    @Override
    public void reset() {
        this.panicFloater = 0.0f;
        this.aggressionFloater = 0.0f;
    }

    @Override // Good practice to mark this as an override now that it's in the interface contract
    public void triggerShock(float intensity) {
        this.panicFloater = Math.min(1.0f, this.panicFloater + intensity);
        this.brainStem.exertEnergy(intensity * 0.2f);
    }

    public void provokeAggression(float amount) {
        this.aggressionFloater = Math.min(1.0f, this.aggressionFloater + amount);
    }

    @Override // Mark as override to fulfill the IAmygdala contract cleanly
    public float getPanicFloater() { return this.panicFloater; }

    @Override // Mark as override to fulfill the IAmygdala contract cleanly
    public float getAggressionFloater() { return this.aggressionFloater; }
}