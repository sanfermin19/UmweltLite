package com.fermine.umweltlite.brain.capability;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import com.fermine.umweltlite.example.brain.UmweltCowBrain;
import com.fermine.umweltlite.example.brain.UmweltPolarBearBrain;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Structural utility managing polymorphic pipeline deserialization mappings.
 * Maps ResourceLocation identifiers to instantiators for smooth NBT reconstruction.
 */
public class UmweltBrainRegistry {

    private static final Map<ResourceLocation, Supplier<IUmweltBrainPipeline>> REGISTRY = new HashMap<>();

    // Standard Resource Keys for your cognitive pipelines
    public static final ResourceLocation POLAR_BEAR_BRAIN = ResourceLocation.fromNamespaceAndPath(UmweltLite.MODID, "polar_bear_brain");
    public static final ResourceLocation COW_BRAIN = ResourceLocation.fromNamespaceAndPath(UmweltLite.MODID, "cow_brain");

    static {
        // Register default brain factories
        register(POLAR_BEAR_BRAIN, UmweltPolarBearBrain::new);
        register(COW_BRAIN, UmweltCowBrain::new);
    }

    /**
     * Explicitly registers a brain architecture mapping profile.
     */
    public static void register(ResourceLocation id, Supplier<IUmweltBrainPipeline> factory) {
        REGISTRY.put(id, factory);
    }

    /**
     * Reconstructs a brand-new uninitialized pipeline instance from a stored type ID.
     * Used by the modern NeoForge data attachment deserializer block.
     *
     * @param id The registered pipeline ResourceLocation.
     * @return A fresh implementation instance, or null if no mapping matches.
     */
    public static IUmweltBrainPipeline createPipelineInstance(ResourceLocation id) {
        Supplier<IUmweltBrainPipeline> factory = REGISTRY.get(id);
        if (factory == null) {
            UmweltLite.LOGGER.error("Failed to deserialize cognitive pipeline! Unknown registry identifier: {}", id);
            return null;
        }
        return factory.get();
    }
}