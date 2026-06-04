package com.fermine.umweltlite.brain.impl.sections;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import com.fermine.umweltlite.brain.inter.sections.IBrainStem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BrainStem implements IBrainStem {

    // Core Floaters / Metrics
    private float heartRate = 70.0f;     // Baseline Beats Per Minute (BPM)
    private float exhaustion = 0.0f;    // 0.0 (Fresh) to 1.0 (Completely Spent)
    private boolean isSuffocating = false;

    @Override
    public void tick(LivingEntity host) {
        // 1. Gather raw data from the universe/body
        this.isSuffocating = host.getAirSupply() <= 0;

        // 2. Calculate physical exertion based on actual velocity
        Vec3 deltaMovement = host.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(deltaMovement.x * deltaMovement.x + deltaMovement.z * deltaMovement.z);

        if (horizontalSpeed > 0.1) {
            // Mob is actively moving/running; exert energy
            exertEnergy(0.005f);
        } else {
            // Passive metabolic recovery
            this.exhaustion = Math.max(0.0f, this.exhaustion - 0.002f);
        }

        // 3. Autonomic Heart Rate Simulation
        // Heart rate climbs with exhaustion, spikes during suffocation, and trails off toward baseline
        float targetHeartRate = 70.0f + (this.exhaustion * 80.0f);
        if (this.isSuffocating) {
            targetHeartRate += 50.0f; // Panic spike from lack of oxygen
        }

        // Smooth interpolation so the heart rate adjusts organically over ticks
        this.heartRate += (targetHeartRate - this.heartRate) * 0.1f;
    }

    @Override
    public void reset() {
        this.heartRate = 70.0f;
        this.exhaustion = 0.0f;
        this.isSuffocating = false;
    }

    // Getters and Mutators
    public float getHeartRate() { return this.heartRate; }
    public float getExhaustion() { return this.exhaustion; }
    public boolean isSuffocating() { return this.isSuffocating; }

    public void exertEnergy(float amount) {
        this.exhaustion = Math.min(1.0f, this.exhaustion + amount);
    }
}