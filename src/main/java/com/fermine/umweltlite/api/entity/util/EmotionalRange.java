package com.fermine.umweltlite.api.entity.util;

public record EmotionalRange(float min, float max) {
    public boolean contains(float value) {
        return value >= min && value <= max;
    }

    // Quick helpers
    public static EmotionalRange atLeast(float min) { return new EmotionalRange(min, 1.0f); }
    public static EmotionalRange atMost(float max) { return new EmotionalRange(-1.0f, max); }
}