package com.fermine.umweltlite.engine.sensory.perception;

public record EntityPerception(
        float threat,
        float socialWeight,
        float vitality,
        float formFactor,   // Silhouette size/intimidation
        boolean isApex      // "Apex" logic replaces simple predator check
) { }
