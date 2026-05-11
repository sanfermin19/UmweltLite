package com.fermine.umweltlite.engine.sensory.senses;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.sensory.analyzer.EntityAnalyzer;
import com.fermine.umweltlite.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.engine.sensory.inter.ISensory;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.List;

public class AuditorySense implements ISensory {
    @Override public int scanRate() { return 10; }
    private boolean enabled = true;

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

        double range = 12.0; // The "Bubble" of awareness
        List<LivingEntity> potentialSounds = mob.level().getEntitiesOfClass(
                LivingEntity.class, mob.getBoundingBox().inflate(range)
        );

        for (LivingEntity source : potentialSounds) {
            if (source == mob) continue;

            // Simplified: If it moves, we hear it.
            double speed = source.getDeltaMovement().length();
            if (speed > 0.02) {
                float distSq = (float) mob.distanceToSqr(source);
                float attenuation = 1.0f - Mth.clamp(distSq / (float)(range * range), 0, 1);

                // Volume = Speed * Mass (FormFactor) * Distance
                var entitySignals = EntityAnalyzer.analyze(source, mob);
                float volume = (float)speed * entitySignals.formFactor() * attenuation;

                // Final safety check via our NBT Helper philosophy
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