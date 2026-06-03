package com.fermine.umweltlite.impl.engine.sensory.senses;

import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.impl.engine.sensory.analyzer.BlockAnalyzer;
import com.fermine.umweltlite.impl.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.impl.engine.sensory.inter.ISensory;
import com.fermine.umweltlite.impl.engine.sensory.raycast.RaycastResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Evaluates physiological grounding states and processes immediate subterranean surface blocks.
 */
public class VestibularSense implements ISensory {
    private boolean enabled = true;

    @Override
    public int scanRate() {
        return 5;
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

        Vec3 start = mob.position();
        Vec3 end = start.add(0.0, -1.5, 0.0);

        BlockHitResult hit = mob.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mob
        ));

        if (hit.getType() == HitResult.Type.BLOCK) {
            intake.setRay("ground_anchor", new RaycastResult(
                    hit.getLocation(),
                    hit.getDirection(),
                    hit.getBlockPos(),
                    false,
                    Optional.empty(),
                    false
            ));

            var state = mob.level().getBlockState(hit.getBlockPos());
            if (!state.isAir()) {
                var perception = BlockAnalyzer.analyze(state);
                intake.blocks.add(new SensoryIntake.BlockObservation(
                        hit.getBlockPos().immutable(),
                        state,
                        perception
                ));
            }

            // Map physical drop depths down to the steering register
            double depth = start.y - hit.getLocation().y;
            engine.getSensoryEngine().setSteeringBias((float) depth);

        } else {
            intake.setRay("ground_anchor", RaycastResult.EMPTY);
            engine.getSensoryEngine().setSteeringBias(1.0f); // Default baseline fallback bias
        }
    }
}