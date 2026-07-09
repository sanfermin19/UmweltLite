package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.goals.engine.StorageInsert;
import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import com.fermine.umweltlite.goals.engine.sensory.engine.SensoryIntake.EntityObservation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class SocialProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        applySocialComfort(engine, mob);

        float empathy = PersonalityAPI.getTrait(engine, "empathy");
        float anxiety = PersonalityAPI.getTrait(engine, "anxiety");
        var intake = engine.getSensoryEngine().getIntake();
        var emotions = engine.getEmotionalEngine();

        // 1. EMOTIONAL CONTAGION (No Streams)
        int size = intake.entities.size();
        for (int i = 0; i < size; i++) {
            EntityObservation observation = intake.entities.get(i);
            var targetEntity = observation.entity();
            String uuidKey = targetEntity.getUUID().toString();

            engine.getKnowledgeEngine().getFact(uuidKey).ifPresent(entry -> {
                float threat = entry.value().getFloat("threat_level");
                float social = entry.value().getFloat("social_bond");

                if (social > 0.7f) emotions.modifyState(0.01f * empathy, -0.005f, 0);
                if (threat > 0.6f) emotions.modifyState(-0.01f, 0.02f * anxiety, 0);
            });

            // Herd Stampede check
            if (targetEntity instanceof Mob otherMob && otherMob.getType() == mob.getType()) {
                if (otherMob.isSprinting() || (otherMob instanceof IUmweltEntity ue &&
                        ue.getUmweltEngine().getEmotionalEngine().getArousal() > 0.8f)) {
                    emotions.modifyState(-0.01f, 0.03f * anxiety, 0);
                }
            }
        }

        // 2. FLOCKING NAVIGATION
        if (empathy > 0.5f && emotions.getArousal() < 0.7f) {
            Vec3 socialTarget = KnowledgeAPI.findUmweltTarget(engine);
            if (socialTarget != null) {
                Vec3 steer = socialTarget.subtract(mob.position()).normalize();
                return new StorageInsert(steer, 0, Optional.of("walk"));
            }
        }

        return StorageInsert.idle();
    }

    public void applySocialComfort(UmweltEngine engine, Mob mob) {
        if ((mob.level().getGameTime() + mob.getId()) % 40 != 0) return;

        var intake = engine.getSensoryEngine().getIntake();
        var facts = engine.getKnowledgeEngine().getFactMap();
        float totalComfort = 0.0f;
        int friendCount = 0;

        for (int i = 0; i < intake.entities.size(); i++) {
            String biasKey = "bias_" + net.minecraft.world.entity.EntityType.getKey(intake.entities.get(i).entity().getType());
            if (facts.containsKey(biasKey)) {
                float valenceBias = facts.get(biasKey).value().getFloat("v");
                if (valenceBias > 0.5f) {
                    totalComfort += valenceBias;
                    friendCount++;
                }
            }
        }

        if (friendCount > 0) {
            float empathy = PersonalityAPI.getTrait(engine, "empathy");
            float recoveryRate = (totalComfort / friendCount) * empathy * 0.02f;

            engine.getEmotionalEngine().modifyState(recoveryRate, -recoveryRate * 3, 0.005f);

            if (engine.getEmotionalEngine().getArousal() > 0.6f && mob.getRandom().nextFloat() < 0.15f) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART,
                            mob.getX(), mob.getY() + 1.5, mob.getZ(), 1, 0.1, 0.1, 0.1, 0);
                }
            }
        }
    }

    @Override
    public int priority() { return 40; } // Will override baseline wander, but yields to Steering
}