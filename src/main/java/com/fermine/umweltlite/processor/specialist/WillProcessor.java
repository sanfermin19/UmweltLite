package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.engine.StorageInsert;
import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class WillProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        float arousal = snapshot.emotionalState().arousal();
        float valence = snapshot.emotionalState().valence();

        Vec3 finalDrive = Vec3.ZERO;
        float jumpUrge = 0.0f;
        Optional<String> anim = Optional.empty();

        // 1. HIGH-LEVEL STATE SELECTION
        if (arousal > 0.85f && valence < 0.3f) {
            // PANIC: Fast, frantic movement
            finalDrive = calculatePanicVector(mob).scale(1.2);
            anim = Optional.of("panic_run");
            jumpUrge = 0.5f;
        }
        else if (valence < 0.4f && !snapshot.sensoryIntake().entities.isEmpty()) {
            // CONFLICT: Aggression or Fear-driven spacing
            finalDrive = handleValenceConflict(snapshot, mob, valence, arousal);
        }
        else if (arousal < 0.3f && mob.getRandom().nextFloat() < 0.02f) {
            // IDLE: Low-energy wandering
            finalDrive = calculateBaseWander(mob).scale(0.5);
            anim = Optional.of("walk");
        }

        // 2. SAFETY CHECK: Environmental Awareness (Water/Void)
        if (finalDrive.lengthSqr() > 1.0E-4) {
            BlockPos futurePos = BlockPos.containing(mob.position().add(finalDrive.scale(1.5)));
            if (!mob.level().getBlockState(futurePos).getFluidState().isEmpty()) {
                finalDrive = Vec3.ZERO; // Stop if about to walk into water/lava
            }
        }

        return new StorageInsert(finalDrive, jumpUrge, anim);
    }

    private Vec3 handleValenceConflict(StorageRetrieval snapshot, Mob mob, float valence, float arousal) {
        var targetObs = snapshot.sensoryIntake().entities.get(0);
        var target = targetObs.entity();
        Vec3 diff = target.position().subtract(mob.position());
        double dist = diff.length();

        // AGGRESSION: If arousal is high and valence is negative, we move to strike
        if (valence < 0.4f && arousal > 0.6f) {
            if (dist < 2.5f) {
                mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                // Damage logic handled here or in a dedicated CombatProcessor
                return Vec3.ZERO;
            }
            return diff.normalize().scale(1.0);
        }

        // AVOIDANCE: Keeping a "personal bubble"
        if (dist < 4.0f) {
            return diff.reverse().normalize().scale(0.7);
        }

        return Vec3.ZERO;
    }

    private Vec3 calculatePanicVector(Mob mob) {
        // Run roughly forward but with a slight random jitter to look "panicked"
        float jitter = (mob.getRandom().nextFloat() - 0.5f) * 0.5f;
        return mob.getLookAngle().add(jitter, 0, jitter).normalize();
    }

    private Vec3 calculateBaseWander(Mob mob) {
        // Pick a random direction on the horizontal plane
        double angle = mob.getRandom().nextDouble() * Math.PI * 2;
        return new Vec3(Math.cos(angle), 0, Math.sin(angle));
    }

    @Override
    public int priority() {
        return 10;
    }
}