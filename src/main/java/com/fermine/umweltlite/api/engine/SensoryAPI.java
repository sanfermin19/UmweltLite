package com.fermine.umweltlite.api.engine;


import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.memory.memory.Memory;
import com.fermine.umweltlite.engine.sensory.inter.ISensory;

import java.util.List;
import java.util.Optional;

public class SensoryAPI {

    /**
     * Finds a specific sense instance by its class.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ISensory> Optional<T> getSense(UmweltEngine engine, Class<T> senseClass) {
        return engine.getSensoryEngine().getSenses().stream()
                .filter(s -> s.getClass().equals(senseClass))
                .map(s -> (T) s)
                .findFirst();
    }

    /**
     * Custom implementation note: To actually "toggle" a sense, your specific
     * sense classes (like OpticalSense) will need a 'boolean enabled' field
     * and an override for 'isEnabled()'.
     */
    public static void setSenseEnabled(UmweltEngine engine, Class<? extends ISensory> senseClass, boolean enabled) {
    }

    /**
     * Directly injects a raw steering bias.
     * Use 1.0 for hard right, -1.0 for hard left.
     */
    public static void applySteeringBias(UmweltEngine engine, float bias) {
        engine.getSensoryEngine().setSteeringBias(bias);
    }

    /**
     * Clears all memories matching a specific tag.
     * The "Selective Amnesia" hook.
     */
    public static void wipeSpecificMemories(UmweltEngine engine, String contextKey, String contextValue) {
        // Query the MemoryEngine for all matching records
        List<Memory> toDelete = engine.getMemoryEngine().query(contextKey, contextValue);

        for (Memory m : toDelete) {
            // Tell the MemoryEngine to forget this specific instance
            engine.getMemoryEngine().forget(m);
        }
    }
}