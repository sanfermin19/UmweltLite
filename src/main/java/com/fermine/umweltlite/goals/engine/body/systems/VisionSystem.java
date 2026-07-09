package com.fermine.umweltlite.goals.engine.body.systems;

import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.body.inter.IBodySystem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class VisionSystem implements IBodySystem {

    @Override
    public Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        return Vec3.ZERO; // Looking somewhere doesn't add physical mechanical energy to vectors
    }

    @Override
    public void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        var intake = engine.getSensoryEngine().getIntake();
        var center = intake.getRay("center");

        // 1. Explicit Vector Tracking Focus
        if (center != null && center.isValid()) {
            Vec3 target = center.hitLocation();
            if (target != null) {
                mob.getLookControl().setLookAt(target.x, target.y, target.z);
                return; // Targeted look has lock priority
            }
        }

        // 2. Fallback to Social Awareness Observation
        if (!intake.entities.isEmpty()) {
            var firstObservation = intake.entities.getFirst();
            if (firstObservation != null && firstObservation.entity() != null) {
                mob.getLookControl().setLookAt(firstObservation.entity(), 30.0F, 30.0F);
            }
        }
    }

    @Override
    public float getSystemPriority() {
        return 2.0f; // Tracking updates execute post-movement modifications
    }
}