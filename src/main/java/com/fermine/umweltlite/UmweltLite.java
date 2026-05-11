package com.fermine.umweltlite;

import com.fermine.umweltlite.api.registry.UmweltRegistryHelper;
import com.fermine.umweltlite.api.command.UmweltCommand;
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fermine.umweltlite.registry.UmweltEntityRegistry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * UmweltLite: A performance-optimized AI framework focusing on
 * Valence, Arousal, and Memory Appraisal.
 */
@Mod(UmweltLite.MODID)
public class UmweltLite {
    public static final String MODID = "umweltlite";

    public UmweltLite(IEventBus modBus) {
        PerceptionRegistry.register(modBus);
        UmweltEntityRegistry.ENTITIES.register(modBus);
        modBus.addListener(this::registerAttributes);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(UmweltEntityRegistry.UMWELT_SHEEP.get(),
                Sheep.createAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 2.0D) // 1 Heart of damage
                        .build());

        event.put(UmweltEntityRegistry.UMWELT_PIG.get(),
                Pig.createAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 3.0D) // Pigs are beefier! 1.5 Hearts
                        .build());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        UmweltCommand.register(event.getDispatcher());
    }

    @EventBusSubscriber(modid = UmweltLite.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            UmweltRegistryHelper.registerAllRenderers(event);
        }
    }
}