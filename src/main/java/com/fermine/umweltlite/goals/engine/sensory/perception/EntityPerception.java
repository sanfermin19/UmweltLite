package com.fermine.umweltlite.goals.engine.sensory.perception;

public record EntityPerception(
        float threatLevel,
        float socialValue,
        float vitality,
        float formFactor,
        boolean isApex
) {
    public static final EntityPerception EMPTY = new EntityPerception(0.0f, 0.0f, 0.0f, 0.0f, false);
}