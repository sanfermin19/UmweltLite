package com.fermine.umweltlite.api.entity.util;

import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

public class UmweltVisualAPI {

    private static final String VISUALS_DISABLED_TAG = "UmweltVisualsDisabled";

    /**
     * Toggles whether the mob displays emotional particles.
     */
    public static void setVisualsEnabled(IUmweltEntity umweltMob, boolean enabled) {
        if (umweltMob instanceof Mob mob) {
            mob.getPersistentData().putBoolean(VISUALS_DISABLED_TAG, !enabled);
        }
    }

    public static boolean areVisualsEnabled(IUmweltEntity umweltMob) {
        if (umweltMob instanceof Mob mob) {
            return !mob.getPersistentData().getBoolean(VISUALS_DISABLED_TAG);
        }
        return true;
    }

    /**
     * Evaluates emotional state and pushes the corresponding visual packet to the client.
     */
    public static void triggerEmotionalVisual(IUmweltEntity umweltMob) {
        if (!(umweltMob instanceof Mob mob) || !areVisualsEnabled(umweltMob)) return;

        UmweltEngine engine = umweltMob.getUmweltEngine();
        if (engine == null) return;

        ParticleOptions particle = getParticleOptions(engine);

        if (particle != null && !mob.level().isClientSide) {
            mob.level().addParticle(particle,
                    mob.getRandomX(0.5), mob.getRandomY() + 0.5, mob.getRandomZ(0.5), 0, 0, 0);
        }
    }

    private static @Nullable ParticleOptions getParticleOptions(UmweltEngine engine) {
        float v = engine.getEmotionalEngine().getValence();
        float a = engine.getEmotionalEngine().getArousal();

        ParticleOptions particle = null;

        // Using your engine's logic to determine the dominant state
        if (a > 0.7f && v < 0.1f) particle = ParticleTypes.ANGRY_VILLAGER;
        else if (a > 0.7f && v >= 0.1f) particle = ParticleTypes.WITCH;
        else if (v > 0.7f && a < 0.5f) particle = ParticleTypes.HEART;
        else if (v < -0.6f && a < 0.4f) particle = ParticleTypes.SQUID_INK;
        return particle;
    }
}