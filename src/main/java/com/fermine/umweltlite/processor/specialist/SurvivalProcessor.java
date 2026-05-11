package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.engine.StorageInsert;
import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

public class SurvivalProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        var intake = engine.getSensoryEngine().getIntake();
        var appraisal = engine.getPersonality().appraise(intake);

        // 1. PHYSIOLOGICAL FEEDBACK (Routing through UmweltAPI)
        if (appraisal.threatWeight() > 0.1f) {
            float valenceDelta = -0.01f * appraisal.threatWeight();
            float arousalDelta = 0.05f * appraisal.threatWeight();

            // Using the API ensures safety and potential hook-ins for other systems
            UmweltAPI.broadcastArousal(mob, snapshot.emotionalState().arousal() + arousalDelta);
            UmweltAPI.broadcastValence(mob, snapshot.emotionalState().arousal() + arousalDelta);
        }

        // 2. CURIOSITY (Mood Boost)
        if (appraisal.curiosityWeight() > 0.5f) {
            UmweltAPI.broadcastArousal(mob, snapshot.emotionalState().arousal() + 0.01f);
        }

        // 3. ACTION: Panic Response
        if (appraisal.threatWeight() > 0.8f) {
            Vec3 escapeOrigin;

            var primaryThreat = intake.entities.stream()
                    .filter(obs -> obs.perception().threat() > 0.5f)
                    .max(Comparator.comparingDouble(obs -> obs.perception().threat()));

            if (primaryThreat.isPresent()) {
                escapeOrigin = primaryThreat.get().entity().position();
            } else {
                var centerRay = intake.getRay("center");
                escapeOrigin = centerRay.isValid() ? centerRay.hitLocation() : mob.position().add(mob.getLookAngle());
            }

            // Calculate the vector pointing AWAY from the danger
            Vec3 escapeVec = mob.position().subtract(escapeOrigin).normalize().scale(1.2);

            // Higher jump urge if arousal is maxed out
            float jumpUrge = snapshot.emotionalState().arousal() > 0.9f ? 0.8f : 0.4f;

            return new StorageInsert(
                    escapeVec,
                    jumpUrge,
                    Optional.of("panic_run")
            );
        }

        return StorageInsert.idle();
    }

    @Override
    public int priority() {
        return 80;
    }
}