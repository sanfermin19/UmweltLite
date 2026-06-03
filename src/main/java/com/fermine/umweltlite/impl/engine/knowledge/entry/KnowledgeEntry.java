package com.fermine.umweltlite.impl.engine.knowledge.entry;

import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.nbt.CompoundTag;

/**
 * Structural container encapsulating a persistent item of intelligence or historical coordinate data.
 */
public record KnowledgeEntry(CompoundTag value, float confidence, long tickCreated) {

    public KnowledgeEntry {
        // Safe verification fallback to wipe out broken numbers right at building boundary
        confidence = UmweltNBTUtils.safeFloat(confidence, 0.0f);
        if (value == null) {
            value = new CompoundTag();
        }
    }

    /**
     * Inspects data internals thoroughly to identify and quarantine poisoned vector or component floats.
     */
    public boolean isDataValid() {
        if (this.value == null) return false;

        // Deep verification sweep of coordinate structures
        if (this.value.contains("x") && !Double.isFinite(this.value.getDouble("x"))) return false;
        if (this.value.contains("y") && !Double.isFinite(this.value.getDouble("y"))) return false;
        return !this.value.contains("z") || Double.isFinite(this.value.getDouble("z"));
    }

    /**
     * Calculates active degradation values utilizing natural exponential half-life computations.
     */
    public float getCurrentConfidence(long currentTick, long halfLife) {
        if (halfLife <= 0L) return 0.0f;
        long age = Math.max(0L, currentTick - this.tickCreated);
        // Using Math.exp for the decay curve
        return this.confidence * (float) Math.exp(-(double) age / (double) halfLife);
    }

    /**
     * Determines whether this historical snapshot has existed beyond its maximum configuration parameters.
     */
    public boolean isStale(long currentTick, long lifespan) {
        return (currentTick - this.tickCreated) > lifespan;
    }

    /**
     * Overridden for clean console logging during /umwelt dump operations.
     */
    @Override
    public String toString() {
        // Records use the component name as the method (tickCreated(), not getTickCreated())
        return String.format("KnowledgeEntry{Confidence: %.2f, CreatedAt: %d, Data: %s}",
                this.confidence(),
                this.tickCreated(),
                this.value().getAsString());
    }
}