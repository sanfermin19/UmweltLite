package com.fermine.umweltlite.impl.engine.sensory.senses;

import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.impl.engine.sensory.analyzer.EntityAnalyzer;
import com.fermine.umweltlite.impl.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.impl.engine.sensory.inter.ISensory;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * Scans the immediate bounding box for kinetic energy spikes and acoustic emissions.
 */
public class AuditorySense implements ISensory {
    private boolean enabled = true;

    @Override
    public int scanRate() {
        return 10; // Throttled processing window to save CPU overhead
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void pulse(Mob mob, UmweltEngine engine, SensoryIntake intake, int tickCount) {
        if (!shouldScan(tickCount)) return;

        double range = 12.0;
        List<LivingEntity> potentialSounds = mob.level().getEntitiesOfClass(
                LivingEntity.class, mob.getBoundingBox().inflate(range)
        );

        for (LivingEntity source : potentialSounds) {
            if (source == null || source == mob) continue;

            double speed = source.getDeltaMovement().length();
            if (speed > 0.02) {
                float distSq = (float) mob.distanceToSqr(source);
                float attenuation = 1.0f - Mth.clamp(distSq / (float) (range * range), 0.0f, 1.0f);

                // Objective physical tracking matrices
                var entitySignals = EntityAnalyzer.analyze(source, mob);
                float volume = (float) speed * entitySignals.formFactor() * attenuation;

                volume = UmweltNBTUtils.safeFloat(volume, 0.0f);

                if (volume > 0.1f) {
                    intake.entities.add(new SensoryIntake.EntityObservation(
                            source, entitySignals, volume, "audio"
                    ));
                }
            }
        }
    }
}