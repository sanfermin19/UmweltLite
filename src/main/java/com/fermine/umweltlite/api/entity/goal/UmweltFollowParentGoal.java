package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.entity.IUmweltEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

public class UmweltFollowParentGoal extends Goal {
    private final Mob child;
    private Mob parent;
    private int timeToRecalcPath;

    public UmweltFollowParentGoal(Mob child) {
        this.child = child;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (child.getAgeScale() >= 0) return false; // Not a baby

        // FIXED: Throttle the incredibly expensive AABB entity scanning lookups!
        if (child.getRandom().nextInt(reducedTickDelay(20)) != 0) return false;

        this.parent = child.level().getNearestEntity(
                child.getClass(),
                TargetingConditions.DEFAULT,
                child, child.getX(), child.getY(), child.getZ(),
                child.getBoundingBox().inflate(16.0)
        );

        return this.parent != null && child.distanceToSqr(this.parent) > 9.0;
    }

    @Override
    public boolean canContinueToUse() {
        if (child.getAgeScale() >= 0) return false;
        if (!this.parent.isAlive()) return false;

        double distSqr = child.distanceToSqr(this.parent);
        // Keep following until we are close enough (3 blocks away) or get completely separated (16+ blocks away)
        return distSqr >= 9.0 && distSqr <= 256.0;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.parent = null;
        this.child.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.parent == null) return;

        // Look at the parent entity constantly
        this.child.getLookControl().setLookAt(this.parent, 30.0F, 30.0F);

        // FIXED: Do not spam pathfinding calculations every single tick! Recalculate along an interval.
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = reducedTickDelay(10 + this.child.getRandom().nextInt(10));
            this.child.getNavigation().moveTo(this.parent, 1.2);
        }

        // Emotional Contagion Engine Integration
        if (parent instanceof IUmweltEntity ueParent && child instanceof IUmweltEntity ueChild) {
            var pEmotions = ueParent.getUmweltEngine().getEmotionalEngine();
            var cEmotions = ueChild.getUmweltEngine().getEmotionalEngine();

            float learningRate = 0.001f; // Subtle, but adds up over time
            float vDiff = pEmotions.getValence() - cEmotions.getValence();
            float aDiff = pEmotions.getArousal() - cEmotions.getArousal();

            // Blend the child's mood into the parent's mental envelope
            cEmotions.modifyState(vDiff * learningRate, aDiff * learningRate, 0);
        }
    }
}