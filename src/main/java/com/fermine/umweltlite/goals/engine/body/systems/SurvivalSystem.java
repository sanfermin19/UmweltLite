package com.fermine.umweltlite.goals.engine.body.systems;

import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.body.inter.IBodySystem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class SurvivalSystem implements IBodySystem {

    @Override
    public Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        Vec3 drive = Vec3.ZERO;
        var intake = engine.getSensoryEngine().getIntake();

        // 1. REFLEX: Edge / Drop-off Protection
        var ground = intake.getRay("ground_anchor");
        if (ground != null && ground.isMiss() && mob.onGround()) {
            Vec3 counterForce = mob.getDeltaMovement().reverse();
            if (counterForce.lengthSqr() > 0.001) {
                drive = drive.add(counterForce.scale(2.0));
            }
        }

        // 2. WHISKERS: Ray-based Obstacle Steer Evacuation
        var left = intake.getRay("left_whisker");
        var right = intake.getRay("right_whisker");
        var center = intake.getRay("center");

        Vec3 lookAngle = mob.getLookAngle();

        if (left != null && left.isBlocked()) {
            drive = drive.add(lookAngle.yRot((float) Math.toRadians(90)).scale(1.2));
        }
        if (right != null && right.isBlocked()) {
            drive = drive.add(lookAngle.yRot((float) Math.toRadians(-90)).scale(1.2));
        }

        // 3. CENTER COLLISION: Immediate Surface-Normal Repulsion
        if (center != null && center.isBlocked() && center.sideHit() != null) {
            drive = drive.add(Vec3.atLowerCornerOf(center.sideHit().getNormal()).scale(1.5));
        }

        // 4. DAMAGE REACTION: Safe Flee Calculation
        if (mob.getLastHurtByMob() != null && (mob.tickCount - mob.getLastHurtByMobTimestamp() < 100)) {
            Vec3 flee = mob.position().subtract(mob.getLastHurtByMob().position());
            double fleeDistSqr = flee.lengthSqr();
            if (fleeDistSqr > 0.01) {
                // Handcrafted normalization optimization to safely guard division by zero
                drive = drive.add(flee.scale(1.0 / Math.sqrt(fleeDistSqr)).scale(2.0));
            }
        }

        return drive;
    }

    @Override
    public void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        if (mob.isInWater() && !mob.canBreatheUnderwater()) {
            mob.setJumping(true); // Hard panic survival trigger to hit surface air pockets
        }
    }

    @Override
    public float getSystemPriority() {
        return 0.0f; // Critical reflexes evaluate before general motor behaviors
    }
}