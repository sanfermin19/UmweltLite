package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.engine.StorageInsert;
import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.knowledge.entry.KnowledgeEntry;
import com.fermine.umweltlite.engine.memory.memory.Memory;
import com.fermine.umweltlite.processor.UmweltProcessor;
import com.fermine.umweltlite.engine.sensory.engine.SensoryIntake.BlockObservation;
import com.fermine.umweltlite.engine.sensory.engine.SensoryIntake.EntityObservation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;

public class KnowledgeProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        // 0. ACTIVATE PASSIVE RECOVERY (Fixes the "never used" warning)
        applyPassiveRecovery(engine, mob);

        var intake = engine.getSensoryEngine().getIntake();
        var knowledge = engine.getKnowledgeEngine();
        var memory = engine.getMemoryEngine();
        var personality = engine.getPersonality();
        long time = mob.level().getGameTime();

        // 1. SPATIAL ENCODING
        int rangeX = personality.getTrait("detail_focus") > 0.5f ? 5 : 10;
        for (BlockObservation observation : intake.blocks) {
            if (mob.getRandom().nextFloat() < 0.05f) {
                BlockPos generalizedPos = new BlockPos(
                        (observation.pos().getX() / rangeX) * rangeX,
                        (observation.pos().getY() / 3) * 3,
                        (observation.pos().getZ() / rangeX) * rangeX
                );
                CompoundTag blockData = new CompoundTag();
                blockData.putString("id", observation.state().getBlock().toString());
                knowledge.insertSpatial(generalizedPos, new KnowledgeEntry(blockData, 0.5f, time));
            }
        }

        // 2. ENTITY & SOCIAL ENCODING
        for (EntityObservation obs : intake.entities) {
            CompoundTag entityData = new CompoundTag();
            entityData.putString("type", obs.entity().getType().toString());

            // Fact-checking: Store the UUID with the current perception
            knowledge.insertFact(obs.entity().getUUID().toString(),
                    new KnowledgeEntry(entityData, obs.perception().threat(), time));

            // Record memory
            CompoundTag context = new CompoundTag();
            context.putString("entity_type", EntityType.getKey(obs.entity().getType()).toString());
            memory.record(new Memory(time, obs.entity().position(),
                    snapshot.emotionalState(), obs.intensity(), context));
        }

        // 3. ENVIRONMENTAL FEEDBACK
        if (intake.worldState != null) {
            float weatherStress = intake.worldState.weatherIntensity() * 0.01f;
            float anxietyFactor = personality.getTrait("anxiety");
            float darkPanic = (1.0f - intake.worldState.lightLevel()) * (anxietyFactor * 0.01f);

            // We use the variable now!
            float newArousal = snapshot.emotionalState().arousal() + weatherStress + darkPanic;
            UmweltAPI.broadcastArousal(mob, newArousal);
        }

        // 4. MEMORY CONSOLIDATION
        if (time % 100 == 0) {
            for (Memory m : new ArrayList<>(memory.getShortTermMemories())) {
                float strength = m.getRetention(time, 6000);
                if (strength > 0.8f || m.isSignificant()) {
                    String entityTypeStr = m.context().getString("entity_type");
                    if (!entityTypeStr.isEmpty()) {
                        // REINFORCE: This is how biases are updated over time
                        KnowledgeAPI.setEmotionalBias(engine, mob.getType(),
                                m.emotionalImpact().valence(),
                                m.emotionalImpact().arousal());
                    }
                    knowledge.insertSpatial(BlockPos.containing(m.location()),
                            new KnowledgeEntry(m.context(), strength, time));
                }
            }
        }

        // 5. SOCIAL INTENT (Updated to check KnowledgeEngine Facts for Biases)
        return intake.entities.stream()
                .filter(obs -> obs.entity() != null && getValenceBiasFor(engine, obs.entity().getType()) > 0.6f)
                .findFirst()
                .map(friend -> {
                    if (mob.distanceTo(friend.entity()) > 10.0f) {
                        return StorageInsert.move(friend.entity().position(), mob.position(), "social_urge");
                    }
                    return StorageInsert.idle();
                })
                .orElse(StorageInsert.idle());
    }

    public float getValenceBiasFor(UmweltEngine engine, EntityType<?> type) {
        String key = "bias_" + EntityType.getKey(type);
        return engine.getKnowledgeEngine().getFact(key)
                .map(entry -> entry.value().getFloat("v"))
                .orElse(0.0f);
    }

    public void applyPassiveRecovery(UmweltEngine engine, Mob mob) {
        if (mob.level().getGameTime() % 100 != 0) return;

        float currentValence = engine.getEmotionalEngine().getValence();
        float currentArousal = engine.getEmotionalEngine().getArousal(); // Now used in logic below

        // Homeostasis: Drift back to 0.5
        if (currentValence < 0.5f) {
            float recovery = 0.005f; // Baseline slow crawl

            // Environmental Therapy
            if (mob.level().isDay() && mob.level().canSeeSky(mob.blockPosition())) {
                recovery += 0.015f;
            }

            // Only lower arousal if they aren't actively being chased (Arousal < 0.8)
            float arousalDrop = (currentArousal > 0.1f && currentArousal < 0.8f) ? -0.01f : 0f;

            engine.getEmotionalEngine().modifyState(recovery, arousalDrop, 0.005f);
        }
    }

    @Override
    public int priority() { return 40; }
}