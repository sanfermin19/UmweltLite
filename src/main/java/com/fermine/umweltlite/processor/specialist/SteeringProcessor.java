package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.engine.StorageInsert;
import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SteeringProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        var intake = engine.getSensoryEngine().getIntake();

        // 1. DATA GATHERING: Check the "Whiskers" and Ground
        var centerRay = intake.getRay("center");
        var leftRay = intake.getRay("left");
        var rightRay = intake.getRay("right");
        var downRay = intake.getRay("down"); // Detecting cliffs/voids

        // 2. VECTOR MATH: Establish coordinate space
        Vec3 look = mob.getLookAngle();
        // Perpendicular vector for side-stepping
        Vec3 leftSteer = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 avoidanceVector = Vec3.ZERO;

        // 3. LOGIC: CLIFF SAFETY (Highest Physical Priority)
        // If the 'down' ray is missing or showing a drop > 3 blocks
        if (downRay.isValid() && downRay.hitLocation().distanceTo(mob.position()) > 3.0) {
            // Force a stop or immediate reverse to prevent falling
            return new StorageInsert(look.reverse().scale(0.5), 0.0f, Optional.of("stop"));
        }

        // 4. LOGIC: OBSTACLE AVOIDANCE
        if (centerRay.isBlocked()) {
            // If the center is hit, we look to the whiskers to decide which way to turn
            if (!leftRay.isBlocked()) {
                avoidanceVector = leftSteer.scale(0.6); // Turn Left
            } else if (!rightRay.isBlocked()) {
                avoidanceVector = leftSteer.scale(-0.6); // Turn Right
            } else {
                avoidanceVector = look.reverse().scale(0.4); // Completely blocked: Back up
            }
        }
        else if (leftRay.isBlocked()) {
            avoidanceVector = leftSteer.scale(-0.5); // Nudge Right
        }
        else if (rightRay.isBlocked()) {
            avoidanceVector = leftSteer.scale(0.5);  // Nudge Left
        }

        // 5. OUTPUT: Apply the Steering Force
        if (avoidanceVector.lengthSqr() > 1.0E-4) {
            return new StorageInsert(avoidanceVector, 0.0f, Optional.empty());
        }

        return StorageInsert.idle();
    }

    @Override
    public int priority() {
        return 100;
    }
}