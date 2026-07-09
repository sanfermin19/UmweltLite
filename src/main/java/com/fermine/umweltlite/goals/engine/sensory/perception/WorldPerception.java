package com.fermine.umweltlite.goals.engine.sensory.perception;

public record WorldPerception(
        float lightLevel,
        float exposure,
        float timeSignal,
        float weatherIntensity
) {
    public static final WorldPerception EMPTY = new WorldPerception(0.5f, 0.0f, 0.5f, 0.0f);

    /**
     * Determines if weather density parameters pass threshold criteria.
     */
    public boolean isStorming() {
        return this.weatherIntensity > 0.4f;
    }
}