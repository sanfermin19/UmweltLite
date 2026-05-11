package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.world.entity.Entity;
import java.util.Optional;

public class UmweltAPI {

    public static Optional<UmweltEngine> getEngine(Entity entity) {
        if (entity instanceof IUmweltEntity umweltMob) {
            return Optional.ofNullable(umweltMob.getUmweltEngine());
        }
        return Optional.empty();
    }

    /**
     * Broadcasts shifts in Arousal (Intensity).
     */
    public static void broadcastArousal(Entity entity, float targetValue) {
        getEngine(entity).ifPresent(engine -> setArousal(engine, targetValue));
    }

    /**
     * Broadcasts shifts in Valence (Mood).
     */
    public static void broadcastValence(Entity entity, float targetValue) {
        getEngine(entity).ifPresent(engine -> setValence(engine, targetValue));
    }

    /**
     * Directly force a specific intensity state across all axes.
     */
    public static void setArousal(UmweltEngine engine, float targetValue) {
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 0.0f);
        float delta = safeTarget - ee.getArousal();
        ee.modifyState(0, delta, 0); // Only shift arousal (middle param)
    }

    /**
     * Directly force a specific emotional state across all axes.
     */
    public static void setValence(UmweltEngine engine, float targetValue) {
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 0.0f);
        float delta = safeTarget - ee.getValence();
        ee.modifyState(delta, 0, 0); // Only shift valence (first param)
    }
}