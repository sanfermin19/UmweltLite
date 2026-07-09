package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.goals.engine.StorageInsert;
import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.knowledge.entry.KnowledgeEntry;
import com.fermine.umweltlite.goals.engine.memory.memory.Memory;
import com.fermine.umweltlite.processor.UmweltProcessor;
import com.fermine.umweltlite.goals.engine.sensory.engine.SensoryIntake.BlockObservation;
import com.fermine.umweltlite.goals.engine.sensory.engine.SensoryIntake.EntityObservation;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public class KnowledgeProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        applyPassiveRecovery(engine, mob);

        var intake = engine.getSensoryEngine().getIntake();
        var knowledge = engine.getKnowledgeEngine();
        var memory = engine.getMemoryEngine();
        var personality = engine.getPersonality();
        long time = mob.level().getGameTime();

        // 1. ENCODE SPACE
        int rangeX = personality.getTrait("detail_focus") > 0.5f ? 5 : 10;
        int blockCount = intake.blocks.size();
        for (int i = 0; i < blockCount; i++) {
            BlockObservation observation = intake.blocks.get(i);
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

        // 2. ENCODE SOCIAL
        int entityCount = intake.entities.size();
        for (int i = 0; i < entityCount; i++) {
            EntityObservation obs = intake.entities.get(i);
            CompoundTag entityData = new CompoundTag();
            entityData.putString("type", EntityType.getKey(obs.entity().getType()).toString());

            knowledge.insertFact(obs.entity().getUUID().toString(),
                    new KnowledgeEntry(entityData, obs.perception().threatLevel(), time));

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
            float newArousal = snapshot.emotionalState().arousal() + weatherStress + darkPanic;
            UmweltAPI.broadcastArousal(mob, newArousal);
        }

        // 4. MEMORY CONSOLIDATION (Staggered by Entity ID to prevent tick lag spikes)
        if ((time + mob.getId()) % 100 == 0) {
            var shortTermMems = memory.getShortTermMemories();
            for (Memory m : shortTermMems) {
                float strength = m.getRetention(time, 6000);

                if (strength > 0.8f || m.isSignificant()) {
                    String entityTypeStr = m.context().getString("entity_type");
                    if (!entityTypeStr.isEmpty()) {
                        KnowledgeAPI.setEmotionalBias(engine, mob.getType(),
                                m.emotionalImpact().valence(),
                                m.emotionalImpact().arousal());
                    }
                    knowledge.insertSpatial(BlockPos.containing(m.location()),
                            new KnowledgeEntry(m.context(), strength, time));
                }
            }
        }

        // Knowledge doesn't move the body.
        return StorageInsert.idle();
    }

    public void applyPassiveRecovery(UmweltEngine engine, Mob mob) {
        if ((mob.level().getGameTime() + mob.getId()) % 100 != 0) return;

        float currentValence = engine.getEmotionalEngine().getValence();
        float currentArousal = engine.getEmotionalEngine().getArousal();

        if (currentValence < 0.5f) {
            float recovery = 0.005f;
            if (mob.level().isDay() && mob.level().canSeeSky(mob.blockPosition())) {
                recovery += 0.015f;
            }
            float arousalDrop = (currentArousal > 0.1f && currentArousal < 0.8f) ? -0.01f : 0f;
            engine.getEmotionalEngine().modifyState(recovery, arousalDrop, 0.005f);
        }
    }

    @Override
    public int priority() { return 10; } // Runs first. Sets up data for others to read.
}