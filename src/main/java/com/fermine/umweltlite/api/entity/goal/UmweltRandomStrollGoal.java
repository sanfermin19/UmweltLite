package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;


public class UmweltRandomStrollGoal extends Goal {
    private final PathfinderMob mob;
    private final UmweltEngine engine;
    private final double speedModifier; // Added this
    private Vec3 target;

    // Standard constructor (Default speed)
    public UmweltRandomStrollGoal(IUmweltEntity umweltMob) {
        this(umweltMob, 1.0D);
    }

    public UmweltRandomStrollGoal(IUmweltEntity umweltMob, double speedModifier) {
        this.mob = (PathfinderMob) umweltMob;
        this.engine = umweltMob.getUmweltEngine();
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (engine.getEmotionalEngine().getEnergy() < 0.2f) return false;
        if (mob.getRandom().nextInt(reducedTickDelay(120)) != 0) return false;

        this.target = findUmweltTarget();
        return this.target != null;
    }

    @Override
    public void start() {
        // Now using the speed modifier here!
        this.mob.getNavigation().moveTo(
                target.x, target.y, target.z,
                getSpeedBasedOnArousal() * speedModifier
        );
    }

    private double getSpeedBasedOnArousal() {
        float arousal = engine.getEmotionalEngine().getArousal();
        // Base 0.8 + arousal drift, capped at 1.0
        return Math.min(0.8D + (arousal * 0.2D), 1.0D);
    }

    private Vec3 findUmweltTarget() {
        // Attempt to pull a target from the KnowledgeAPI (Spatial Map)
        Vec3 rememberedPos = KnowledgeAPI.findUmweltTarget(this.engine);

        if (rememberedPos != null) {
            return rememberedPos;
        }

        // Explores if no memory map information
        return DefaultRandomPos.getPos(this.mob, 10, 7);
    }
}