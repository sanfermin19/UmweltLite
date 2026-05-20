package com.fermine.umweltlite.engine.sensory.senses;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.sensory.analyzer.BlockAnalyzer;
import com.fermine.umweltlite.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.engine.sensory.inter.ISensory;
import com.fermine.umweltlite.engine.sensory.raycast.RaycastResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class VestibularSense implements ISensory {
    @Override
    public int scanRate() { return 5; }

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

        // 1. Precise Raycast for the "Spatial Anchor"
        Vec3 start = mob.position();
        Vec3 end = start.add(0, -1.5, 0);

        BlockHitResult hit = mob.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            // A. Store the ray for the Body Engine (Prevents "Poofing")
            intake.setRay("ground_anchor", new RaycastResult(
                    hit.getLocation(),
                    hit.getDirection(),
                    hit.getBlockPos(),
                    false,
                    Optional.empty(),
                    false // isMiss = false
            ));

            // B. Perform the Block Analysis from your original logic
            var state = mob.level().getBlockState(hit.getBlockPos());
            if (!state.isAir()) {
                var perception = BlockAnalyzer.analyze(state);
                // Using the updated intake.blocks list
                intake.blocks.add(new SensoryIntake.BlockObservation(
                        hit.getBlockPos().immutable(),
                        state,
                        perception
                ));
            }

            // C. Calculate Steering Bias (Depth sensing)
            double depth = start.y - hit.getLocation().y;
            engine.getSensoryEngine().setSteeringBias((float) depth);

        } else {
            // Clear if airborne to avoid stale data
            intake.setRay("ground_anchor", RaycastResult.EMPTY);
            engine.getSensoryEngine().setSteeringBias(1.0f);
        }
    }
}