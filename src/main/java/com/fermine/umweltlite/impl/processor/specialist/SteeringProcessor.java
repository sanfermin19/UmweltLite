package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.impl.engine.StorageInsert;
import com.fermine.umweltlite.impl.engine.StorageRetrieval;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class SteeringProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        var intake = engine.getSensoryEngine().getIntake();
        var centerRay = intake.getRay("center");
        var leftRay = intake.getRay("left");
        var rightRay = intake.getRay("right");
        var downRay = intake.getRay("down");

        Vec3 look = mob.getLookAngle();
        Vec3 leftSteer = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 avoidanceVector = Vec3.ZERO;

        // 1. CLIFF SAFETY
        if (downRay.isValid() && downRay.hitLocation().distanceToSqr(mob.position()) > 9.0) { // 3.0 * 3.0
            return new StorageInsert(look.reverse().scale(0.5), 0, Optional.of("stop"));
        }

        // 2. OBSTACLE AVOIDANCE
        if (centerRay.isBlocked()) {
            if (!leftRay.isBlocked()) avoidanceVector = leftSteer.scale(0.6);
            else if (!rightRay.isBlocked()) avoidanceVector = leftSteer.scale(-0.6);
            else avoidanceVector = look.reverse().scale(0.4);
        }
        else if (leftRay.isBlocked()) avoidanceVector = leftSteer.scale(-0.5);
        else if (rightRay.isBlocked()) avoidanceVector = leftSteer.scale(0.5);

        if (avoidanceVector.lengthSqr() > 1.0E-4) {
            return new StorageInsert(avoidanceVector, 0, Optional.empty());
        }

        return StorageInsert.idle();
    }

    @Override
    public int priority() { return 100; } // Highest. The physics barrier has the final say.
}