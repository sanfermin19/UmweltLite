package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.StorageInsert;
import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class SocialProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        // 0. SOCIAL RECOVERY: The first thing we do is check if friends make us feel better
        applySocialComfort(engine, mob);

        float empathy = PersonalityAPI.getTrait(engine, "empathy");
        float anxiety = PersonalityAPI.getTrait(engine, "anxiety");

        var intake = engine.getSensoryEngine().getIntake();
        var emotions = engine.getEmotionalEngine();

        // 1. DYNAMIC EVALUATION: Scaled by Personality
        intake.entities.forEach(observation -> {
            var targetEntity = observation.entity();
            String uuidKey = targetEntity.getUUID().toString();

            engine.getKnowledgeEngine().getFact(uuidKey).ifPresent(entry -> {
                var data = entry.value(); // Matches your KnowledgeEntry field
                float threat = data.getFloat("threat_level");
                float social = data.getFloat("social_bond");

                if (social > 0.7f) {
                    float warmthGain = 0.01f * empathy;
                    emotions.modifyState(warmthGain, -0.005f, 0);
                }

                if (threat > 0.6f) {
                    float panicShift = 0.02f * anxiety;
                    emotions.modifyState(-0.01f, panicShift, 0);
                }
            });

            // 2. SOCIAL CONTAGION: The "Stampede" Mechanic
            if (targetEntity instanceof Mob otherMob && otherMob.getType() == mob.getType()) {
                // If a neighbor is panicking (High Arousal), it spreads to this mob
                if (otherMob.isSprinting() || (otherMob instanceof IUmweltEntity ue && ue.getUmweltEngine().getEmotionalEngine().getArousal() > 0.8f)) {
                    // Panic spreads faster in anxious herds
                    emotions.modifyState(-0.01f, 0.03f * anxiety, 0);
                }
            }
        });

        // 3. FLOCKING NAVIGATION
        Vec3 socialTarget = KnowledgeAPI.findUmweltTarget(engine);

        // Only seek social targets if we aren't currently terrified
        if (socialTarget != null && empathy > 0.5f && emotions.getArousal() < 0.7f) {
            return StorageInsert.move(socialTarget, mob.position(), "social_flocking");
        }

        return StorageInsert.idle();
    }

    public void applySocialComfort(UmweltEngine engine, Mob mob) {
        long time = mob.level().getGameTime();
        if (time % 40 != 0) return; // Pulse every 2 seconds

        var intake = engine.getSensoryEngine().getIntake();
        var facts = engine.getKnowledgeEngine().getFactMap();

        float totalComfort = 0.0f;
        int friendCount = 0;

        for (var obs : intake.entities) {
            String biasKey = "bias_" + net.minecraft.world.entity.EntityType.getKey(obs.entity().getType());

            if (facts.containsKey(biasKey)) {
                CompoundTag data = facts.get(biasKey).value();
                float valenceBias = data.getFloat("v");

                if (valenceBias > 0.5f) {
                    totalComfort += valenceBias;
                    friendCount++;
                }
            }
        }

        if (friendCount > 0) {
            float empathy = PersonalityAPI.getTrait(engine, "empathy");
            // Recovery is stronger the more friends you have around (Safety in Numbers)
            float recoveryRate = (totalComfort / friendCount) * empathy * 0.02f;

            engine.getEmotionalEngine().modifyState(
                    recoveryRate,      // Rise Valence
                    -recoveryRate * 3,  // Drop Arousal (Calm down significantly)
                    0.005f             // Passive energy gain
            );

            if (engine.getEmotionalEngine().getArousal() > 0.6f && mob.getRandom().nextFloat() < 0.15f) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HEART,
                            mob.getX(), mob.getY() + 1.5, mob.getZ(), 1, 0.1, 0.1, 0.1, 0);
                }
            }
        }
    }

    @Override
    public int priority() {
        return 60;
    }
}