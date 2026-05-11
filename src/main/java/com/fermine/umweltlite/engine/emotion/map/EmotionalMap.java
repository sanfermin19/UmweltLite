package com.fermine.umweltlite.engine.emotion.map;

import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.util.Mth;

public record EmotionalMap(float valence, float arousal, float energy) {

    public EmotionalMap {
        // Compact validation
        valence = Mth.clamp(UmweltNBTUtils.safeFloat(valence, 0.0f), -1.0f, 1.0f);
        arousal = Mth.clamp(UmweltNBTUtils.safeFloat(arousal, 0.0f), 0.0f, 1.0f);
        energy = Mth.clamp(UmweltNBTUtils.safeFloat(energy, 1.0f), 0.0f, 1.0f);
    }

    public EmotionalMap add(float v, float a, float e) {
        return new EmotionalMap(this.valence + v, this.arousal + a, this.energy + e);
    }
}