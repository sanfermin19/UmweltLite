package com.fermine.umweltlite.brain.capability;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.brain.impl.UmweltBrain;
import com.fermine.umweltlite.impl.registry.UmweltEntityRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltCapabilityRegistry {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // This tells NeoForge: "Whenever someone asks for the BRAIN capability on an UmweltCow,
        // lazily construct and return a new instance of UmweltBrain specifically for that cow."
        event.registerEntity(
                UmweltCapabilities.BRAIN,
                UmweltEntityRegistry.UMWELT_COW.get(),
                (cow, context) -> new UmweltBrain()
        );
    }
}