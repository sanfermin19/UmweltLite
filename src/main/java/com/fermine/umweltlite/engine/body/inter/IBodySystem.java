package com.fermine.umweltlite.engine.body.inter;


import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.sensory.raycast.RaycastResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public interface IBodySystem {
    /**
     * Now passes the full SensoryIntake so systems can access Whiskers,
     * Ground Anchors, and Entities.
     */
    Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot);

    void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot);

    default float getSystemPriority() { return 1.0f; }
}