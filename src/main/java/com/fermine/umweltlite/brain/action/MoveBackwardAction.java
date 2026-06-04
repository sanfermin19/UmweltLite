package com.fermine.umweltlite.brain.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class MoveBackwardAction implements IBrainAction {
    private final double speedModifier;

    public MoveBackwardAction(float speedModifier) {
        this.speedModifier = speedModifier;
    }

    @Override
    public String getActionId() {
        return "move_backward";
    }

    @Override
    public void execute(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            // Calculate a point 2 blocks directly behind it
            float yaw = mob.getYRot();
            double lookX = Math.sin(Math.toRadians(yaw)) * 2.0;
            double lookZ = -Math.cos(Math.toRadians(yaw)) * 2.0;

            Vec3 targetPos = mob.position().add(lookX, 0, lookZ);

            mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, this.speedModifier);
        }
    }
}