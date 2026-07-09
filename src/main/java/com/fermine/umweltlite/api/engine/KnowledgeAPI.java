package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.knowledge.entry.KnowledgeEntry;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KnowledgeAPI {

    /**
     * Registers a persistent emotional disposition bias profile against an explicit EntityType profile.
     * Bypasses standard aging mechanics completely due to absolute confidence metric pinning.
     */
    public static void setEmotionalBias(UmweltEngine engine, EntityType<?> type, float valence, float arousal) {
        if (engine == null || type == null) return;

        String key = "bias_" + EntityType.getKey(type);
        CompoundTag data = new CompoundTag();

        data.putFloat("v", UmweltNBTUtils.safeFloat(valence, 0.0f));
        data.putFloat("a", UmweltNBTUtils.safeFloat(arousal, 0.0f));

        setPermanentFact(engine, key, data);
    }

    /**
     * Injects a raw factual entry with maximum structural permanence.
     */
    public static void setPermanentFact(UmweltEngine engine, String key, CompoundTag data) {
        if (engine == null || key == null || data == null) return;

        long time = engine.getMob().level().getGameTime();
        engine.getKnowledgeEngine().insertFact(key, new KnowledgeEntry(data, 1.0f, time));
    }

    /**
     * Registers localized geographical memory maps with customizable processing confidences.
     */
    public static void setSpatialMemory(UmweltEngine engine, BlockPos pos, CompoundTag metadata, float confidence) {
        if (engine == null || pos == null || metadata == null) return;

        long time = engine.getMob().level().getGameTime();
        float safeConfidence = UmweltNBTUtils.safeFloat(confidence, 0.5f);

        engine.getKnowledgeEngine().insertSpatial(pos, new KnowledgeEntry(metadata, safeConfidence, time));
    }

    /**
     * Wipes a precise targeted factual tracking entry key string.
     */
    public static void wipeFact(UmweltEngine engine, String key) {
        if (engine == null || key == null) return;
        engine.getKnowledgeEngine().removeFact(key);
    }

    /**
     * Safely executes deep cognitive system clears without tripping unmodifiable structure exceptions.
     */
    public static void clearAllKnowledge(UmweltEngine engine) {
        if (engine == null) return;
        engine.getKnowledgeEngine().clearAllMemoryChannels();
    }

    /**
     * Queries the entity spatial database to provide optimized destination tracking possibilities.
     * Returns null if no valid historical locations pass criteria.
     */
    public static Vec3 findUmweltTarget(UmweltEngine engine) {
        if (engine == null) return null;

        var spatialMap = engine.getKnowledgeEngine().getSpatialMap();
        Mob mob = engine.getMob();

        if (!spatialMap.isEmpty() && mob.getRandom().nextFloat() < 0.7f) {
            List<BlockPos> knownPositions = new ArrayList<>(spatialMap.keySet());
            BlockPos targetPos = knownPositions.get(mob.getRandom().nextInt(knownPositions.size()));

            if (targetPos != null && !targetPos.equals(mob.blockPosition())) {
                return Vec3.atBottomCenterOf(targetPos);
            }
        }

        return null;
    }

    // --- Modern Epistemological & Confidence Evaluation Hooks ---

    /**
     * Computes the current decaying confidence value of a semantic fact.
     * Returns 0.0f if the fact does not exist within the entity's memory files.
     */
    public static float getFactConfidence(UmweltEngine engine, String key, long halfLife) {
        if (engine == null || key == null) return 0.0f;
        KnowledgeEntry entry = engine.getKnowledgeEngine().getFactMap().get(key);
        if (entry == null) return 0.0f;

        long gameTime = engine.getMob().level().getGameTime();
        return entry.getCurrentConfidence(gameTime, halfLife);
    }

    /**
     * Computes the current decaying confidence value of a specific spatial coordinate block memory.
     * Returns 0.0f if the block location is unregistered.
     */
    public static float getSpatialConfidence(UmweltEngine engine, BlockPos pos, long halfLife) {
        if (engine == null || pos == null) return 0.0f;
        KnowledgeEntry entry = engine.getKnowledgeEngine().getSpatialMap().get(pos);
        if (entry == null) return 0.0f;

        long gameTime = engine.getMob().level().getGameTime();
        return entry.getCurrentConfidence(gameTime, halfLife);
    }

    /**
     * Evaluates whether a generic semantic fact has crossed its biological age threshold limit.
     */
    public static boolean isFactStale(UmweltEngine engine, String key, long lifespan) {
        if (engine == null || key == null) return true;
        KnowledgeEntry entry = engine.getKnowledgeEngine().getFactMap().get(key);
        if (entry == null) return true;

        long gameTime = engine.getMob().level().getGameTime();
        return entry.isStale(gameTime, lifespan);
    }

    /**
     * Evaluates whether a tracking spatial block memory pos has crossed its cognitive retention threshold.
     */
    public static boolean isSpatialStale(UmweltEngine engine, BlockPos pos, long lifespan) {
        if (engine == null || pos == null) return true;
        KnowledgeEntry entry = engine.getKnowledgeEngine().getSpatialMap().get(pos);
        if (entry == null) return true;

        long gameTime = engine.getMob().level().getGameTime();
        return entry.isStale(gameTime, lifespan);
    }

    /**
     * Retrieves a read-only view of the permanent facts map.
     */
    public static Map<String, KnowledgeEntry> getFactMap(UmweltEngine engine) {
        return engine.getKnowledgeEngine().getFactMap();
    }

    /**
     * Retrieves a read-only view of the spatial knowledge map.
     */
    public static Map<BlockPos, KnowledgeEntry> getSpatialMap(UmweltEngine engine) {
        return engine.getKnowledgeEngine().getSpatialMap();
    }
}