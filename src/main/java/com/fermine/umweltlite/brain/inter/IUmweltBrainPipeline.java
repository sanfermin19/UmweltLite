package com.fermine.umweltlite.brain.inter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * The unified contract governing single-class stateful cognitive pipelines.
 * Ensures data attachments, entity tick loops, and rendering shaders have standard accessors.
 */
public interface IUmweltBrainPipeline {

    /**
     * Executes the main environmental perception, emotional distillation, and action-routing loops.
     */
    void tickBrainPipeline(Mob host);

    /**
     * Applies internal physiological rules or biological dampeners (e.g. sleep cycles, exhaustion).
     */
    void regulate(Mob host);

    /**
     * Arbitrates democratic priority voting across registered action behaviors.
     */
    void decide(Mob host);

    // --- Global Emotional Registers (Circumplex Model) ---
    float getValence();
    float getArousal();

    // --- Core Triad Domain Registers ---
    float getSurvival();
    float getSelfInterest();
    float getAnalytical();

    /**
     * Queries an immutable personality trait index for this specific organism.
     * @param trait The string key representing the trait (e.g. "bravery", "anxiety", "empathy").
     * @return The baseline multiplier score bounded between 0.0F and 1.0F.
     */
    float getPersonalityTrait(String trait);

    /**
     * @return The text representation of the current processing task to display on diagnostic screens.
     */
    String getActiveStageName();

    /**
     * @return The unique registered ResourceLocation mapping ID used for polymorphic disk serialization lookups.
     */
    ResourceLocation getBrainTypeId();

    /**
     * Serializes internal floating registers and personality seeds to disk.
     */
    CompoundTag serializePipeline();

    /**
     * Restores cognitive states and traits from disk upon entity loading.
     */
    void deserializePipeline(CompoundTag tag);
}