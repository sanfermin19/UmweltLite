package com.fermine.umweltlite.engine.memory.memory;


import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
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

    public boolean isSignificant() {
        // Significant memories are high arousal or high valence (very good or very scary)
        return emotionalImpact != null && (emotionalImpact.arousal() > 0.6f || Math.abs(emotionalImpact.valence()) > 0.7f);
    }

    public boolean isExpired(long currentTick, long lifespan) {
        return (currentTick - timestamp) > lifespan;
    }
}