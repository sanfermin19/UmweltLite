package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.goals.engine.StorageInsert;
import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class SurvivalProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        var intake = engine.getSensoryEngine().getIntake();
        var appraisal = engine.getPersonality().appraise(intake);

        if (appraisal.threatWeight() > 0.1f) {
            float delta = appraisal.threatWeight();
            UmweltAPI.broadcastArousal(mob, snapshot.emotionalState().arousal() + (0.05f * delta));
            UmweltAPI.broadcastValence(mob, snapshot.emotionalState().valence() - (0.01f * delta));
        }

        if (appraisal.curiosityWeight() > 0.5f) {
            UmweltAPI.broadcastArousal(mob, snapshot.emotionalState().arousal() + 0.01f);
        }

        // PANIC RESPONSE (Overrides lower priorities)
        if (appraisal.threatWeight() > 0.8f) {
            Vec3 escapeOrigin = null;
            double maxThreat = -1.0;

            // Highly optimized iteration to find the worst threat
            int size = intake.entities.size();
            for (int i = 0; i < size; i++) {
                var obs = intake.entities.get(i);
                if (obs.perception().threatLevel() > maxThreat) {
                    maxThreat = obs.perception().threatLevel();
                    escapeOrigin = obs.entity().position();
                }
            }

            if (escapeOrigin == null) {
                var centerRay = intake.getRay("center");
                escapeOrigin = centerRay.isValid() ? centerRay.hitLocation() : mob.position().add(mob.getLookAngle());
            }

            Vec3 escapeVec = mob.position().subtract(escapeOrigin).normalize().scale(1.2);
            int jumpUrge = snapshot.emotionalState().arousal() > 0.9f ? 1 : 0; // Fixed from float to int!

            return new StorageInsert(escapeVec, jumpUrge, Optional.of("panic_run"));
        }

        return StorageInsert.idle();
    }

    @Override
    public int priority() { return 80; } // Instinct. Overrides Social and Will.

    @Override
    public boolean isExhaustionExempt() { return true; } // Must always run — exhaustion never blocks survival.
}