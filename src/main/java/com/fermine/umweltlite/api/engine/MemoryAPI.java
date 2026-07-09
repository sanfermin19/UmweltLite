package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.goals.engine.memory.memory.Memory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Global API hook for interacting with an entity's short-term episodic memory buffer.
 * Provides safe modification, injection, querying, and analytical metrics.
 */
public class MemoryAPI {

    /**
     * Injects a synthetic memory directly into the buffer.
     * Use this to simulate "Hallucinations" or "Implanted Instructions".
     */
    public static void injectMemory(UmweltEngine engine, Vec3 pos, EmotionalMap impact, CompoundTag context) {
        if (engine == null || engine.getMemoryEngine() == null) return;

        Memory synthetic = new Memory(
                engine.getMob().level().getGameTime(),
                pos,
                impact,
                1.0f,
                context
        );
        engine.getMemoryEngine().record(synthetic);
    }

    /**
     * Re-weights an existing memory by altering its emotional resonance.
     * Useful for "Gaslighting" a mob into thinking a traumatic event wasn't that bad.
     */
    public static void reevaluateMemory(UmweltEngine engine, String contextKey, String contextValue, float newValence, float newArousal) {
        if (engine == null || engine.getMemoryEngine() == null) return;

        engine.getMemoryEngine().modifyMemories(
                m -> m.matchesContext(contextKey, contextValue),
                old -> new Memory(
                        old.timestamp(),
                        old.location(),
                        new EmotionalMap(newValence, newArousal, old.emotionalImpact() != null ? old.emotionalImpact().energy() : 1.0f),
                        old.initialConfidence(),
                        old.context()
                )
        );
    }

    /**
     * Exposes generic mutation over the short-term buffer via the API layer.
     */
    public static void modifyMemories(UmweltEngine engine, Predicate<Memory> filter, Function<Memory, Memory> modifier) {
        if (engine == null || engine.getMemoryEngine() == null || filter == null || modifier == null) return;
        engine.getMemoryEngine().modifyMemories(filter, modifier);
    }

    /**
     * Clears all memories matching a specific tag value.
     * The standard "Selective Amnesia" hook.
     */
    public static void wipeSpecificMemories(UmweltEngine engine, String contextKey, String contextValue) {
        if (engine == null || engine.getMemoryEngine() == null) return;
        engine.getMemoryEngine().removeIf(m -> m.matchesContext(contextKey, contextValue));
    }

    /**
     * Wipes memories based on a custom evaluation predicate.
     */
    public static void wipeMemoriesIf(UmweltEngine engine, Predicate<Memory> condition) {
        if (engine == null || engine.getMemoryEngine() == null || condition == null) return;
        engine.getMemoryEngine().removeIf(condition);
    }

    /**
     * Fetches the most recent memories from the engine buffer up to a specified count.
     */
    public static List<Memory> getRecent(UmweltEngine engine, int count) {
        if (engine == null || engine.getMemoryEngine() == null) return List.of();
        return engine.getMemoryEngine().getRecentMemories(count);
    }

    /**
     * Streams through the episodic buffer to check if any active memory matches a specific context token.
     */
    public static boolean hasMemoryWithContext(UmweltEngine engine, String contextKey, String contextValue) {
        if (engine == null || engine.getMemoryEngine() == null) return false;
        return engine.getMemoryEngine().getShortTermMemories().stream()
                .anyMatch(m -> m.matchesContext(contextKey, contextValue));
    }

    /**
     * Counts how many registered memories match the given analytical predicate.
     * FIXED: Changed the broken '0M' syntax error over to a standard long literal '0L'.
     */
    public static long countMemoriesMatching(UmweltEngine engine, Predicate<Memory> criteria) {
        if (engine == null || engine.getMemoryEngine() == null || criteria == null) return 0L;
        return engine.getMemoryEngine().getShortTermMemories().stream()
                .filter(criteria)
                .count();
    }

    /**
     * Analytical Hook: Evaluates if a specific memory instance is categorized as "Significant".
     */
    public static boolean isSignificant(Memory memory) {
        return memory != null && memory.isSignificant();
    }

    /**
     * Analytical Hook: Evaluates if a given memory instance has surpassed its standard decay lifespan.
     */
    public static boolean isExpired(UmweltEngine engine, Memory memory, long customLifespan) {
        if (engine == null || memory == null) return true;
        return memory.isExpired(engine.getMob().level().getGameTime(), customLifespan);
    }

    /**
     * Analytical Hook: Evaluates the remaining retention strength coefficient [0.0f to 1.0f] of a memory.
     */
    public static float getRetentionStrength(UmweltEngine engine, Memory memory, long lifespan) {
        if (engine == null || memory == null) return 0.0f;
        return memory.getRetention(engine.getMob().level().getGameTime(), lifespan);
    }
}