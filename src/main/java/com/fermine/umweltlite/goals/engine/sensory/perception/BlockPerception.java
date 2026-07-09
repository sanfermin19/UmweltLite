package com.fermine.umweltlite.goals.engine.sensory.perception;

public record BlockPerception(
        float visualNoise,
        float intensity,
        float saturation,
        float roughness,
        float dangerRating,
        float interestRating
) {
    public static final BlockPerception EMPTY = new BlockPerception(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
}