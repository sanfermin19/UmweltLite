package com.fermine.umweltlite.impl.engine.memory.memory;

import com.fermine.umweltlite.impl.engine.emotion.map.EmotionalMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public record Memory(
        long timestamp,
        Vec3 location,
        EmotionalMap emotionalImpact,
        float initialConfidence,
        CompoundTag context
) {
    /**
     * Calculates the current "strength" of the memory based on its age.
     * @param currentTick Current game time
     * @param lifespan How long until it's completely forgotten
     */
    public float getRetention(long currentTick, long lifespan) {
        float age = (float) (currentTick - timestamp);
        float decay = 1.0f - (age / lifespan);
        return Math.max(0, decay * initialConfidence);
    }

    /**
     * Significant memories are high arousal or high valence (very good or very scary)
     */
    public boolean isSignificant() {
        return emotionalImpact != null &&
                (emotionalImpact.arousal() > 0.6f || Math.abs(emotionalImpact.valence()) > 0.7f);
    }

    /**
     * Checks if the memory age has exceeded the permitted lifespan threshold.
     */
    public boolean isExpired(long currentTick, long lifespan) {
        return (currentTick - timestamp) > lifespan;
    }

    /**
     * Helper to safely verify context tag strings without throwing null-pointers on missing keys.
     */
    public boolean matchesContext(String key, String expectedValue) {
        return context != null && context.contains(key) && context.getString(key).equals(expectedValue);
    }
}