package com.fermine.umweltlite;

import com.fermine.umweltlite.api.command.UmweltCommand;
import com.fermine.umweltlite.brain.capability.UmweltAttachments; // Don't forget this import!
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fermine.umweltlite.impl.registry.UmweltEntityRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(UmweltLite.MODID)
public class UmweltLite {
    public static final String MODID = "umweltlite";

    public UmweltLite(IEventBus modBus) {
        // Register Registries
        UmweltAttachments.ATTACHMENT_TYPES.register(modBus); // Added this!
        PerceptionRegistry.register(modBus);
        UmweltEntityRegistry.ENTITIES.register(modBus);

        // Register Global Events
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        UmweltCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}