package com.fermine.umweltlite.engine.body.systems;


import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.body.inter.IBodySystem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class VisionSystem implements IBodySystem {
    @Override
    public Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        return Vec3.ZERO; // Eyes don't move the feet
    }

    @Override
    public void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        var intake = engine.getSensoryEngine().getIntake();
        var center = intake.getRay("center");

        // If we are looking at something, lock the head to it
        if (center.isValid()) {
            Vec3 target = center.hitLocation();
            mob.getLookControl().setLookAt(target.x, target.y, target.z);
        } else if (!intake.entities.isEmpty()) {
            // Otherwise, look at the first interesting entity we "hear" or see
            var firstEntity = intake.entities.getFirst().entity();
            mob.getLookControl().setLookAt(firstEntity, 30.0F, 30.0F);
        }
    }
}