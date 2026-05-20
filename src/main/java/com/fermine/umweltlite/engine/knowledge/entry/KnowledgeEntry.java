package com.fermine.umweltlite.engine.knowledge.entry;

import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.nbt.CompoundTag;

public record KnowledgeEntry(CompoundTag value, float confidence, long tickCreated) {

    public KnowledgeEntry {
        // Instant rejection of NaN math
        confidence = UmweltNBTUtils.safeFloat(confidence, 0.0f);
    }

    public boolean isDataValid() {
        // Deep check for poisoned coordinates in NBT
        if (value.contains("x") && !Double.isFinite(value.getDouble("x"))) return false;
        if (value.contains("y") && !Double.isFinite(value.getDouble("y"))) return false;
        return !value.contains("z") || Double.isFinite(value.getDouble("z"));
    }

    public float getCurrentConfidence(long currentTick, long halfLife) {
        long age = Math.max(0, currentTick - tickCreated);
        return confidence * (float)Math.exp(-(float)age / (float)halfLife);
    }

    public boolean isStale(long currentTick, long lifespan) {
        return (currentTick - tickCreated) > lifespan;
    }
}