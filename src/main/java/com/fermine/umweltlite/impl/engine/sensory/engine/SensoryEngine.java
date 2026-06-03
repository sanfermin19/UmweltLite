package com.fermine.umweltlite.impl.engine.sensory.engine;

import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.impl.engine.sensory.inter.ISensory;
import com.fermine.umweltlite.impl.engine.sensory.senses.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the tick lifecycle and data propagation for the entity sensory array.
 */
public class SensoryEngine {
    private final List<ISensory> senses = new ArrayList<>();
    private final SensoryIntake intake = new SensoryIntake();
    private float steeringBias = 0.0f;

    public SensoryEngine() {
        senses.add(new OpticalSense());
        senses.add(new AuditorySense());
        senses.add(new VestibularSense());
    }

    public void tick(Mob mob, UmweltEngine engine) {
        // Initialize host reference context and sweep old frames
        intake.beginTick(mob);

        for (ISensory sense : senses) {
            if (sense.isEnabled()) {
                sense.pulse(mob, engine, intake, mob.tickCount);

                // Particle debugging execution path (throttled)
                if (engine.isExhausted() && mob.tickCount % 20 == 0 && mob.level() instanceof ServerLevel serverLevel) {
                    spawnSenseParticle(serverLevel, mob, sense);
                }
            }
        }
    }

    private void spawnSenseParticle(ServerLevel level, Mob mob, ISensory sense) {
        double x = mob.getX();
        double y = mob.getEyeY();
        double z = mob.getZ();

        if (sense instanceof OpticalSense) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.1, 0.1, 0.1, 0.02);
        } else if (sense instanceof AuditorySense) {
            level.sendParticles(ParticleTypes.NOTE, x, y + 0.5, z, 1, 0.2, 0.2, 0.2, 0.1);
        } else if (sense instanceof VestibularSense) {
            level.sendParticles(ParticleTypes.CRIT, x, mob.getY(), z, 1, 0.1, 0.0, 0.1, 0.01);
        }
    }

    public List<ISensory> getSenses() {
        return Collections.unmodifiableList(senses);
    }

    public SensoryIntake getIntake() {
        return this.intake;
    }

    public void setSteeringBias(float bias) {
        this.steeringBias = bias;
    }

    public float getSteeringBias() {
        return this.steeringBias;
    }
}