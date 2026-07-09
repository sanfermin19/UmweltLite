package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class UmweltPanicGoal extends Goal {
    private final PathfinderMob mob;
    private final UmweltEngine engine;
    private final double speedModifier;
    private Vec3 panicPos;

    public UmweltPanicGoal(IUmweltEntity umweltMob, double speedModifier) {
        this.mob = (PathfinderMob) umweltMob;
        this.engine = umweltMob.getUmweltEngine();
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (engine.getEmotionalEngine().getArousal() < 0.4f) {
            return false;
        }

        if (mob.getLastHurtByMob() != null || mob.isOnFire()) {
            this.panicPos = DefaultRandomPos.getPos(this.mob, 16, 7);
            return this.panicPos != null;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // If we haven't reached the destination, keep running.
        if (!this.mob.getNavigation().isDone()) {
            return true;
        }

        // If we reached it, High Anxiety mobs might chain another panic path!
        float anxiety = PersonalityAPI.getTrait(engine, "anxiety");
        if (mob.getRandom().nextFloat() < (anxiety * 0.3f)) {
            this.panicPos = DefaultRandomPos.getPos(this.mob, 16, 7);
            if (this.panicPos != null) {
                this.mob.getNavigation().moveTo(panicPos.x, panicPos.y, panicPos.z, getPanicSpeed() * speedModifier);
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(
                panicPos.x,
                panicPos.y,
                panicPos.z,
                getPanicSpeed() * speedModifier
        );
    }

    @Override
    public void stop() {
        this.panicPos = null;
        this.mob.getNavigation().stop();
        engine.getEmotionalEngine().modifyState(-0.1f, -0.05f, -0.2f);
    }

    private double getPanicSpeed() {
        float arousal = engine.getEmotionalEngine().getArousal();
        float bravery = PersonalityAPI.getTrait(engine, "bravery");
        return 1.2D + (arousal * 0.5D) - (bravery * 0.2D);
    }
}