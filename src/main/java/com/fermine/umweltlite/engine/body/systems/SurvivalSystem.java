package com.fermine.umweltlite.engine.body.systems;


import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.body.inter.IBodySystem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class SurvivalSystem implements IBodySystem {

    @Override
    public Vec3 getDriveVector(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        Vec3 drive = Vec3.ZERO;
        var intake = engine.getSensoryEngine().getIntake();

        // 1. REFLEX: Hazard Avoidance (Water)
        // Use the Ground Anchor from Vestibular sense to see if we are about to step into depth
        var ground = intake.getRay("ground_anchor");
        if (ground.isMiss() && mob.onGround()) {
            // We are at an edge! Push back.
            drive = drive.add(mob.getDeltaMovement().reverse().scale(2.0));
        }

        // 2. WHISKERS: Modular Obstacle Avoidance
        // If the left whisker is blocked, steer right. If right is blocked, steer left.
        var left = intake.getRay("left_whisker");
        var right = intake.getRay("right_whisker");
        var center = intake.getRay("center");

        if (left.isBlocked()) drive = drive.add(mob.getLookAngle().yRot((float)Math.toRadians(90)).scale(1.2));
        if (right.isBlocked()) drive = drive.add(mob.getLookAngle().yRot((float)Math.toRadians(-90)).scale(1.2));

        // If center is blocked, we need a hard stop or a random turn
        if (center.isBlocked()) {
            drive = drive.add(Vec3.atLowerCornerOf(center.sideHit().getNormal()).scale(1.5));
        }

        // 3. DAMAGE REACTION
        if (mob.getLastHurtByMob() != null && mob.tickCount - mob.getLastHurtByMobTimestamp() < 100) {
            Vec3 flee = mob.position().subtract(mob.getLastHurtByMob().position());
            // NaN Firewall: Only normalize if distance exists
            if (flee.lengthSqr() > 0.01) {
                drive = drive.add(flee.normalize().scale(2.0));
            }
        }

        return drive;
    }

    @Override
    public void pulse(Mob mob, UmweltEngine engine, StorageRetrieval snapshot) {
        if (mob.isInWater() && !mob.canBreatheUnderwater()) {
            mob.setJumping(true); // Basic struggle reflex
        }
    }
}