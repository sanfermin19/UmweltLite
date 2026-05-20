package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.engine.memory.memory.Memory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MemoryAPI {

    /**
     * Injects a synthetic memory directly into the buffer.
     * Use this to simulate "Hallucinations" or "Implanted Instructions".
     */
    public static void injectMemory(UmweltEngine engine, Vec3 pos, EmotionalMap impact, CompoundTag context) {
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
     * Re-weights an existing memory.
     * Useful for "Gaslighting" a mob into thinking a scary event wasn't that bad.
     */
    public static void reevaluateMemory(UmweltEngine engine, String contextKey, String contextValue, float newValence, float newArousal) {
        List<Memory> matches = engine.getMemoryEngine().query(contextKey, contextValue);
        for (Memory old : matches) {
            Memory rewritten = new Memory(
                    old.timestamp(),
                    old.location(),
                    new EmotionalMap(newValence, newArousal, old.emotionalImpact().energy()),
                    old.initialConfidence(),
                    old.context()
            );
            engine.getMemoryEngine().record(rewritten);
        }
    }


    /**
     * Clears all memories matching a specific tag.
     * The "Selective Amnesia" hook.
     */
    public static void wipeSpecificMemories(UmweltEngine engine, String contextKey, String contextValue) {
        // Now .getShortTermMemories() will resolve
        engine.getMemoryEngine().getShortTermMemories().removeIf(m ->
                m.context().getString(contextKey).equals(contextValue)
        );
    }

    /**
     * Helper to get the raw list if the engine exposes it,
     * otherwise use a getter in the engine.
     */
    public static List<Memory> getRecent(UmweltEngine engine, int count) {
        return engine.getMemoryEngine().getRecentMemories(count);
    }
}