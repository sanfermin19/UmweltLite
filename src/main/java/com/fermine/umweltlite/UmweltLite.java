package com.fermine.umweltlite;

import com.fermine.umweltlite.api.command.UmweltCommand;
import com.fermine.umweltlite.brain.capability.UmweltAttachments;
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fermine.umweltlite.example.UmweltEntityRegistry;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(UmweltLite.MODID)
public class UmweltLite {
    public static final String MODID = "umweltlite";

    // Core structural fix: Exposed the standard Slf4j logging gate
    public static final Logger LOGGER = LogUtils.getLogger();

    public UmweltLite(IEventBus modBus) {
        // Register Registries
        UmweltAttachments.ATTACHMENT_TYPES.register(modBus);
        PerceptionRegistry.register(modBus);
        UmweltEntityRegistry.ENTITIES.register(modBus);

        // Register Global Events
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        UmweltCommand.register(event.getDispatcher(), event.getBuildContext());
    }
}