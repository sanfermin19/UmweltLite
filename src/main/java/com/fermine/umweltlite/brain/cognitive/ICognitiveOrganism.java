package com.fermine.umweltlite.brain.cognitive;

import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Developer-facing API which allows developers to easily extend into their own code.
 * This allows developers to gain access to an EEP-inspired behavioral framework.
 */
public interface ICognitiveOrganism {

    /**
     * @return True if the internal psychological state engine is attached and ticking properly.
     */
    boolean isCognitiveEngineActive();

    /**
     * Safely retrieves the current underlying multi-stage processing pipeline.
     * @return The active cognitive pipeline, or null if the entity hasn't initialized its core.
     */
    @Nullable
    IUmweltBrainPipeline getCognitivePipeline();

    /**
     * A helper shortcut to retrieve the exact type mapping string of the engine.
     * Useful for cross-mod network packets and diagnostic integrations.
     */
    default ResourceLocation getCognitiveTypeId() {
        IUmweltBrainPipeline pipeline = this.getCognitivePipeline();
        return pipeline != null ? pipeline.getBrainTypeId() : ResourceLocation.fromNamespaceAndPath("umwelt", "offline");
    }
}