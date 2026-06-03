package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.impl.engine.sensory.inter.ISensory;

import java.util.List;
import java.util.Optional;

/**
 * Public API boundary for modifying and querying the entity sensory and short-term memory arrays.
 * Tailored for modular goals, script engines, and integration hooks.
 */
public class SensoryAPI {

    /**
     * Finds a specific sense instance by its class structure.
     */
    @SuppressWarnings("unchecked")
    public static <T extends ISensory> Optional<T> getSense(UmweltEngine engine, Class<T> senseClass) {
        if (engine == null || senseClass == null) {
            return Optional.empty();
        }

        List<ISensory> activeSenses = engine.getSensoryEngine().getSenses();
        int size = activeSenses.size();

        for (ISensory sense : activeSenses) {
            if (sense.getClass().equals(senseClass)) {
                return Optional.of((T) sense);
            }
        }
        return Optional.empty();
    }

    /**
     * Toggles the processing state of a specific sense type dynamically.
     */
    public static void setSenseEnabled(UmweltEngine engine, Class<? extends ISensory> senseClass, boolean enabled) {
        if (engine == null || senseClass == null) return;

        getSense(engine, senseClass).ifPresent(sense -> sense.setEnabled(enabled));
    }

    /**
     * Directly injects a raw steering bias into the sensory engine calculations.
     * Use 1.0 for hard right vectors, -1.0 for hard left vectors.
     */
    public static void applySteeringBias(UmweltEngine engine, float bias) {
        if (engine == null) return;
        engine.getSensoryEngine().setSteeringBias(bias);
    }

    /**
     * Clears all short-term memories matching a specific context tag key-value criteria.
     * Serves as the primary public engine hook for selective amnesia triggers.
     */
    public static void wipeSpecificMemories(UmweltEngine engine, String contextKey, String contextValue) {
        if (engine == null || contextKey == null || contextValue == null) return;

        // Optimized execution path: bypass intermediate array allocations by pushing the predicate downstream
        engine.getMemoryEngine().removeIf(memory -> memory.matchesContext(contextKey, contextValue));
    }
}