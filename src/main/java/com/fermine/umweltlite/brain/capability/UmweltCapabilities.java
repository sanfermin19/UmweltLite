package com.fermine.umweltlite.brain.capability;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.brain.inter.IUmweltBrain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class UmweltCapabilities {

    // This defines the capability token. Void means it doesn't require extra context (like a side direction) to fetch.
    public static final EntityCapability<IUmweltBrain, Void> BRAIN = EntityCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(UmweltLite.MODID, "brain"),
            IUmweltBrain.class
    );
}