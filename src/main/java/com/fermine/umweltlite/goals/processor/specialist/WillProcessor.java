package com.fermine.umweltlite.processor.specialist;

import com.fermine.umweltlite.goals.engine.StorageInsert;
import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public class WillProcessor implements UmweltProcessor {

    @Override
    public StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot) {
        float arousal = snapshot.emotionalState().arousal();
        float valence = snapshot.emotionalState().valence();

        // 1. CONFLICT / AGGRESSION
        if (valence < 0.4f && !snapshot.sensoryIntake().entities.isEmpty()) {
            return handleValenceConflict(snapshot, mob, valence, arousal);
        }

        // 2. IDLE WANDERING
        if (arousal < 0.3f && mob.getRandom().nextFloat() < 0.02f) {
            return new StorageInsert(calculateBaseWander(mob).scale(0.5), 0, Optional.of("walk"));
        }

        return StorageInsert.idle();
    }

    private StorageInsert handleValenceConflict(StorageRetrieval snapshot, Mob mob, float valence, float arousal) {
        var targetObs = snapshot.sensoryIntake().entities.getFirst();
        var target = targetObs.entity();

        Vec3 diff = target.position().subtract(mob.position());
        double distSqr = diff.lengthSqr(); // Much faster than .length()

        // High arousal, low valence = FIGHT
        if (valence < 0.4f && arousal > 0.6f) {
            if (distSqr < 6.25) { // 2.5 * 2.5
                mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                return StorageInsert.idle(); // Stop moving to swing
            }
            return new StorageInsert(diff.normalize().scale(1.0), 0, Optional.of("walk"));
        }

        // Personal space bubble (Back away)
        if (distSqr < 16.0) { // 4.0 * 4.0
            return new StorageInsert(diff.reverse().normalize().scale(0.7), 0, Optional.of("walk"));
        }

        return StorageInsert.idle();
    }

    private Vec3 calculateBaseWander(Mob mob) {
        double angle = mob.getRandom().nextDouble() * Math.PI * 2;
        return new Vec3(Math.cos(angle), 0, Math.sin(angle));
    }

    @Override
    public int priority() { return 30; } // Low priority. Easily overridden by Steering/Survival.
}