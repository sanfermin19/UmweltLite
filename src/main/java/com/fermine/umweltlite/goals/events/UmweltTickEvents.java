package com.fermine.umweltlite.goals.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.api.entity.util.UmweltVisualAPI;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltTickEvents {

    @SubscribeEvent
    public static void onUmweltPulse(EntityTickEvent.Post event) {
        // Frequency check
        if (event.getEntity().level().getGameTime() % 20 != 0) return;

        if (event.getEntity() instanceof IUmweltEntity umweltMob && event.getEntity() instanceof Mob) {
            // Simply call the API - the API handles the "is enabled" logic internally
            UmweltVisualAPI.triggerEmotionalVisual(umweltMob);
        }
    }
}