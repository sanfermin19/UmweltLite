package com.fermine.umweltlite.impl.engine.body.inter;

import com.fermine.umweltlite.impl.engine.StorageRetrieval;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public interface IBodySystem {
    /**
     * Executes internal biological/reflex systems, modifies entity state fields, or ticks animations.
     */
    void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot);

    /**
     * Returns a steering or corrective vector to modify the master intent's drive.
     */
    Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot);

    /**
     * Low values run first. Determines internal system execution orders.
     */
    float getSystemPriority();
}