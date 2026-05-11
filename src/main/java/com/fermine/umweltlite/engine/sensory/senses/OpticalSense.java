package com.fermine.umweltlite.engine.sensory.senses;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.sensory.analyzer.WorldAnalyzer;
import com.fermine.umweltlite.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.engine.sensory.inter.ISensory;
import com.fermine.umweltlite.engine.sensory.raycast.RaycastResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class OpticalSense implements ISensory {
    @Override
    public int scanRate() { return 1; } // Optical processing remains per-tick for navigation
    private boolean enabled = true;

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

        // 1. Global Environmental State
        intake.worldState = WorldAnalyzer.analyze(mob);

        // 2. The Vector Fan (Whiskers)
        Vec3 eyePos = mob.getEyePosition();
        float yaw = mob.getViewYRot(1.0F);

        // Cast three rays: Center (0°), Left (-25°), Right (25°)
        castOpticalRay(mob, intake, "center", eyePos, yaw, 0, 16.0);
        castOpticalRay(mob, intake, "left_whisker", eyePos, yaw, -25, 6.0);
        castOpticalRay(mob, intake, "right_whisker", eyePos, yaw, 25, 6.0);
    }

    private void castOpticalRay(Mob mob, SensoryIntake intake, String name, Vec3 start, float yaw, float angleOffset, double range) {
        // Calculate the direction vector for this specific whisker
        Vec3 dir = Vec3.directionFromRotation(0, yaw + angleOffset);
        Vec3 end = start.add(dir.scale(range));

        BlockHitResult hit = mob.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            // Use the new hardened RaycastResult to prevent NaN poisoning
            intake.setRay(name, new RaycastResult(
                    hit.getLocation(),
                    hit.getDirection(),
                    hit.getBlockPos(),
                    false,
                    Optional.empty(),
                    false // isMiss = false
            ));
        } else {
            intake.setRay(name, RaycastResult.EMPTY);
        }
    }
}