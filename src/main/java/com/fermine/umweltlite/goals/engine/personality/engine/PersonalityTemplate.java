package com.fermine.umweltlite.goals.engine.personality.engine;

import java.util.Map;

/**
 * Immutable blueprint defining base behavior baselines and genetic variance parameters.
 */
public record PersonalityTemplate(
        String id,
        Map<String, Float> baseTraits,
        float variance // The maximum bounds a unique DNA seed can bend these base traits
) {
    public PersonalityTemplate {
        // Defensive copy to guarantee absolute immutability across multithreaded mod operations
        baseTraits = Map.copyOf(baseTraits);
    }
}