package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.knowledge.entry.KnowledgeEntry;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Gatekeeper for the mob's long-term memory and facts.
 */
public class KnowledgeAPI {

    /**
     * Injects an Emotional Bias as a "Permanent Fact".
     * High confidence (1.0f) ensures it survives 'Deep Pruning'.
     */
    public static void setEmotionalBias(UmweltEngine engine, EntityType<?> type, float valence, float arousal) {
        String key = "bias_" + EntityType.getKey(type);
        CompoundTag data = new CompoundTag();

        // Hard-clamp biases via NBT Helper to prevent NaN corruption
        data.putFloat("v", UmweltNBTUtils.safeFloat(valence, 0.0f));
        data.putFloat("a", UmweltNBTUtils.safeFloat(arousal, 0.0f));

        setPermanentFact(engine, key, data);
    }

    /**
     * Injects a fact that will not decay naturally.
     */
    public static void setPermanentFact(UmweltEngine engine, String key, CompoundTag data) {
        long time = engine.getMob().level().getGameTime();
        // The KnowledgeEntry constructor will now validate 'data' for finite math
        engine.getKnowledgeEngine().insertFact(key, new KnowledgeEntry(data, 1.0f, time));
    }

    /**
     * Records a spatial memory with validation.
     */
    public static void setSpatialMemory(UmweltEngine engine, BlockPos pos, CompoundTag metadata, float confidence) {
        long time = engine.getMob().level().getGameTime();

        // Safety: ensure confidence is valid before creating the entry
        float safeConfidence = UmweltNBTUtils.safeFloat(confidence, 0.5f);

        engine.getKnowledgeEngine().insertSpatial(pos, new KnowledgeEntry(metadata, safeConfidence, time));
    }

    /**
     * Removes a specific fact from the engine.
     */
    public static void wipeFact(UmweltEngine engine, String key) {
        engine.getKnowledgeEngine().removeFact(key);
    }

    /**
     * Resets the mob's mind entirely.
     */
    public static void clearAllKnowledge(UmweltEngine engine) {
        engine.getKnowledgeEngine().getFactMap().clear();
        engine.getKnowledgeEngine().getSpatialMap().clear();
    }

    /**
     * Resolves a target destination by querying the spatial knowledge map.
     * Returns null if no valid or interesting memories are found.
     */
    public static Vec3 findUmweltTarget(UmweltEngine engine) {
        var spatialMap = engine.getKnowledgeEngine().getSpatialMap(); //
        var mob = engine.getMob(); //

        if (!spatialMap.isEmpty() && mob.getRandom().nextFloat() < 0.7f) {
            // 1. Get all known locations from the KnowledgeEngine
            List<BlockPos> knownPositions = new ArrayList<>(spatialMap.keySet());

            // 2. Selection Logic: Pick a random "remembered" spot
            BlockPos targetPos = knownPositions.get(mob.getRandom().nextInt(knownPositions.size()));

            // 3. Systems Check: Ensure it's not the exact block we are standing on
            if (!targetPos.equals(mob.blockPosition())) {
                return Vec3.atBottomCenterOf(targetPos);
            }
        }

        // 4. Fallback: If the map is empty or the sheep feels adventurous
        return null;
    }
}