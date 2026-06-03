package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class UmweltRandomStrollGoal extends Goal {
    private final PathfinderMob mob;
    private final UmweltEngine engine;
    private final double speedModifier;
    private Vec3 target;

    public UmweltRandomStrollGoal(IUmweltEntity umweltMob, double speedModifier) {
        this.mob = (PathfinderMob) umweltMob;
        this.engine = umweltMob.getUmweltEngine();
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isLeashed()) return false;
        if (engine.getEmotionalEngine().getEnergy() < 0.2f) return false;
        if (mob.getRandom().nextInt(reducedTickDelay(120)) != 0) return false;
        this.target = findUmweltTarget();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isLeashed()) return false;
        return !this.mob.getNavigation().isDone() && engine.getEmotionalEngine().getEnergy() >= 0.15f;
    }

    @Override
    public void start() {
        if (this.target != null) {
            this.mob.getNavigation().moveTo(
                    target.x, target.y, target.z,
                    getSpeedBasedOnArousal() * speedModifier
            );
        }
    }

    @Override
    public void tick() {
        if (mob.getNavigation().isInProgress()) {
            float currentEnergy = engine.getEmotionalEngine().getEnergy();
            EmotionAPI.setEnergy(engine, Math.max(0.0f, currentEnergy - 0.0005f));
        }
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    private double getSpeedBasedOnArousal() {
        float arousal = engine.getEmotionalEngine().getArousal();
        return Math.min(0.8D + (arousal * 0.2D), 1.0D);
    }

    private Vec3 findUmweltTarget() {
        Vec3 rememberedPos = KnowledgeAPI.findUmweltTarget(this.engine);
        if (rememberedPos != null) {
            return rememberedPos;
        }
        return DefaultRandomPos.getPos(this.mob, 10, 7);
    }
}