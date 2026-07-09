package com.fermine.umweltlite.goals.engine.sensory.senses;

import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.sensory.analyzer.WorldAnalyzer;
import com.fermine.umweltlite.goals.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.goals.engine.sensory.inter.ISensory;
import com.fermine.umweltlite.goals.engine.sensory.raycast.RaycastResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Handles directional raycast projection and global environment snapshots.
 */
public class OpticalSense implements ISensory {
    private boolean enabled = true;

    @Override
    public int scanRate() {
        return 1; // Continuous processing required for obstacle avoidance mapping
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void pulse(Mob mob, UmweltEngine engine, SensoryIntake intake, int tickCount) {
        if (!shouldScan(tickCount)) return;

        // 1. Process pure mathematical snapshot of world atmospheric states
        intake.worldState = WorldAnalyzer.analyze(mob);

        // 2. Vector Fan Whisker Calculations
        Vec3 eyePos = mob.getEyePosition();
        float yaw = mob.getViewYRot(1.0F);

        // Map defensive safety rays ahead of the entity path
        castOpticalRay(mob, intake, "center", eyePos, yaw, 0.0f, 16.0);
        castOpticalRay(mob, intake, "left_whisker", eyePos, yaw, -25.0f, 6.0);
        castOpticalRay(mob, intake, "right_whisker", eyePos, yaw, 25.0f, 6.0);
    }

    private void castOpticalRay(Mob mob, SensoryIntake intake, String name, Vec3 start, float yaw, float angleOffset, double range) {
        Vec3 dir = Vec3.directionFromRotation(0.0f, yaw + angleOffset);
        Vec3 end = start.add(dir.scale(range));

        BlockHitResult hit = mob.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            intake.setRay(name, new RaycastResult(
                    hit.getLocation(),
                    hit.getDirection(),
                    hit.getBlockPos(),
                    false,
                    Optional.empty(),
                    false
            ));
        } else {
            intake.setRay(name, RaycastResult.EMPTY);
        }
    }
}