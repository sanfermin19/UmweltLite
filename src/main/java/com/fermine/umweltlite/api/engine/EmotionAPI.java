package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.utils.UmweltNBTUtils;

public class EmotionAPI {

    /**
     * Sets the energy to a specific value by calculating the necessary delta.
     */
    public static void setEnergy(UmweltEngine engine, float targetValue) {
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 1.0f);
        float delta = safeTarget - ee.getEnergy();
        ee.modifyState(0, 0, delta);
    }

    /**
     * Sets the valence to a specific value by calculating the necessary delta.
     */
    public static void setValence(UmweltEngine engine, float targetValue) {
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 0.0f);
        float delta = safeTarget - ee.getValence();
        ee.modifyState(delta, 0, 0);
    }

    /**
     * Sets the arousal to a specific value by calculating the necessary delta.
     */
    public static void setArousal(UmweltEngine engine, float targetValue) {
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 0.0f);
        float delta = safeTarget - ee.getArousal();
        ee.modifyState(0, delta, 0);
    }

    /**
     * Directly force a specific emotional state across all axes.
     */
    public static void setEmotionalState(UmweltEngine engine, float v, float a, float e) {
        setValence(engine, v);
        setArousal(engine, a);
        setEnergy(engine, e);
    }
}