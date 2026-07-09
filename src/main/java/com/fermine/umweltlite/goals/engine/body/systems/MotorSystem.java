package com.fermine.umweltlite.goals.engine.body.systems;

import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.body.inter.IBodySystem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class MotorSystem implements IBodySystem {

    @Override
    public Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        return Vec3.ZERO; // Pure scalar modifier, leaves vector logic to steering systems
    }

    @Override
    public void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        float arousal = snapshot.emotionalState().arousal();
        float energy = snapshot.emotionalState().energy();

        // 1. Adrenaline Boost (Arousal scale)
        double adrenaline = 1.0 + (arousal * 0.6);

        // 2. Fatigue Scaling (Energy drop off)
        double fatigue = energy < 0.2f ? Math.max(0.4, energy * 5.0) : 1.0;
        double finalSpeedModifier = adrenaline * fatigue;

        // Apply dynamically to vanilla pathing without wiping modifiers out completely
        var speedAttribute = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            // Directly hook navigation scaling parameters dynamically per-tick safely
            if (mob.getNavigation().isInProgress()) {
                mob.setSpeed((float) finalSpeedModifier);
            }
        }
    }

    @Override
    public float getSystemPriority() {
        return 1.0f;
    }
}