package com.fermine.umweltlite.engine.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class EngineNBTHelper {

    /**
     * Saves a float to NBT.
     */
    public static void save(CompoundTag tag, String key, float value) {
        tag.putFloat(key, value);
    }

    /**
     * Loads a float with a fallback and a clamp range.
     */
    public static float loadClamped(CompoundTag tag, String key, float min, float max, float defaultValue) {
        if (tag.contains(key)) {
            return Mth.clamp(tag.getFloat(key), min, max);
        }
        return defaultValue;
    }

    /**
     * Specialized for the 0.0 - 1.0 range used in most of your engines.
     */
    public static float loadNormalized(CompoundTag tag, String key, float defaultValue) {
        return loadClamped(tag, key, 0.0f, 1.0f, defaultValue);
    }
}