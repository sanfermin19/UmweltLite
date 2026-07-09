package com.fermine.umweltlite.brain.handler;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.brain.capability.UmweltAttachments;
import com.fermine.umweltlite.example.brain.UmweltPolarBearBrain;
import com.fermine.umweltlite.example.UmweltEntityRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Automates server-side initialization and lazy data attachment bindings
 * for target entities managed by UmweltLite.
 */
@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltAttachmentEventHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Entity entity = event.getEntity();

        if (entity instanceof Mob mob) {
            // If it's your mod's polar bear, automatically provision a new Polar Bear Brain engine instance!
            if (mob.getType() == UmweltEntityRegistry.UMWELT_POLAR_BEAR.get()) {
                if (!mob.hasData(UmweltAttachments.BRAIN_PIPELINE)) {
                    mob.setData(UmweltAttachments.BRAIN_PIPELINE, new UmweltPolarBearBrain());
                    UmweltLite.LOGGER.debug("Successfully bound Cognitive Triad Engine to custom Polar Bear entity: [{}]", mob.getUUID());
                }
            }
        }
    }
}