package com.fermine.umweltlite.api.entity.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class UmweltFollowParentGoal extends Goal {
    private final Mob child;
    private Mob parent;

    public UmweltFollowParentGoal(Mob child) {
        this.child = child;
    }

    @Override
    public boolean canUse() {
        if (child.getAgeScale() >= 0) return false; // Not a baby

        this.parent = child.level().getNearestEntity(child.getClass(),
                net.minecraft.world.entity.ai.targeting.TargetingConditions.DEFAULT,
                child, child.getX(), child.getY(), child.getZ(),
                child.getBoundingBox().inflate(16.0));

        return this.parent != null && child.distanceToSqr(this.parent) > 9.0;
    }

    @Override
    public void tick() {
        if (parent instanceof com.fermine.umweltlite.api.entity.IUmweltEntity ueParent &&
                child instanceof com.fermine.umweltlite.api.entity.IUmweltEntity ueChild) {

            // EMOTIONAL ECHO: The baby slowly drifts toward the parent's mood
            var pEmotions = ueParent.getUmweltEngine().getEmotionalEngine();
            var cEmotions = ueChild.getUmweltEngine().getEmotionalEngine();

            float learningRate = 0.001f; // Subtle, but adds up
            float vDiff = pEmotions.getValence() - cEmotions.getValence();
            float aDiff = pEmotions.getArousal() - cEmotions.getArousal();

            cEmotions.modifyState(vDiff * learningRate, aDiff * learningRate, 0);
        }

        this.child.getNavigation().moveTo(parent, 1.2);
    }
}