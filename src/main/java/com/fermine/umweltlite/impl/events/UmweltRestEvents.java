package com.fermine.umweltlite.impl.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltRestEvents {

    /**
     * Triggered when an Umwelt-enabled entity is in a "Resting" state.
     * We use a tick-based pulse to slowly recover Valence and dump Arousal.
     */
    @SubscribeEvent
    public static void onRestingPulse(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof IUmweltEntity umweltMob && event.getEntity().level().getGameTime() % 40 == 0) {
            UmweltEngine engine = umweltMob.getUmweltEngine();
            if (engine == null) return;

            // Check if the mob is currently executing a RestGoal (or equivalent rest state)
            // You'll want to expose a boolean getter in your IUmweltEntity interface
            if (umweltMob.isResting()) {

                // Recovery is slow and steady
                float currentValence = engine.getEmotionalEngine().getValence();
                float currentArousal = engine.getEmotionalEngine().getArousal();
                float currentEnergy = engine.getEmotionalEngine().getEnergy();

                // Valence recovers to baseline (0.0), Arousal dumps toward calm (0.0), Energy fills up
                EmotionAPI.setValence(engine, Math.min(0.0f, currentValence + 0.05f));
                EmotionAPI.setArousal(engine, Math.max(0.0f, currentArousal - 0.1f));
                EmotionAPI.setEnergy(engine, Math.min(1.0f, currentEnergy + 0.05f));
            }
        }
    }
}