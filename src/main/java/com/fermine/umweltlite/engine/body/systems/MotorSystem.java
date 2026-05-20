package com.fermine.umweltlite.engine.body.systems;

import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.body.inter.IBodySystem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class MotorSystem implements IBodySystem {

    /**
     * Updated: Now matches the new IBodySystem signature.
     * The Motor System remains a pass-through for movement vectors.
     */
    @Override
    public Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        return Vec3.ZERO;
    }

    /**
     * Applies physical speed modifiers based on the internal emotional state.
     */
    @Override
    public void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        // Pull values from the sanitized EmotionalState record
        float arousal = snapshot.emotionalState().arousal();
        float energy = snapshot.emotionalState().energy();

        // 1. Adrenaline Boost (Arousal)
        // Higher arousal increases base movement speed capacity.
        double adrenaline = 1.0 + (arousal * 0.6);

        // 2. Fatigue Penalty (Energy)
        // If energy is below 20%, start scaling speed down significantly.
        double fatigue = energy < 0.2f ? Math.max(0.4, energy * 5.0) : 1.0;

        double finalSpeed = adrenaline * fatigue;

        // Apply to the vanilla navigator
        if (mob.getNavigation().isInProgress()) {
            mob.setSpeed((float) finalSpeed);
        }
    }

    @Override
    public float getSystemPriority() {
        return 1.0f; // Standard priority for base motor control
    }
}