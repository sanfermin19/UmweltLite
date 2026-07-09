package com.fermine.umweltlite.goals.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.processor.ProcessorLoader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Optional;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltJoinEvents {

    /**
     * Ensures that every Umwelt-enabled entity has its engine bound
     * immediately upon entering the level.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        // Use the new API gateway to grab the engine safely
        Optional<UmweltEngine> engineOptional = UmweltAPI.getEngine(entity);

        if (engineOptional.isPresent()) {
            UmweltEngine engine = engineOptional.get();
            IUmweltEntity umweltMob = (IUmweltEntity) entity;

            /*
             * Logic: Check if the mob needs processing.
             * Since we don't have isEngineBound, we can use a flag inside
             * the IUmweltEntity to see if it's currently ticking/processing.
             */
            if (!umweltMob.isProcessing()) {

                // Initialize the loader
                ProcessorLoader.initializeEngine((Mob) umweltMob, engine);

                // Set the flag to true so we don't re-initialize every time it joins
                umweltMob.setProcessing(true);
            }
        }
    }
}