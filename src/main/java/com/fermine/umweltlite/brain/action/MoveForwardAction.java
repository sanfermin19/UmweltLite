package com.fermine.umweltlite.brain.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class MoveForwardAction implements IBrainAction {
    private final double speedModifier;

    public MoveForwardAction(double speedModifier) {
        this.speedModifier = speedModifier;
    }

    @Override
    public String getActionId() {
        return "move_forward";
    }

    @Override
    public void execute(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            // Calculate a target point 3 blocks ahead based on current head/body yaw rotation
            float yaw = mob.getYRot();
            double lookX = -Math.sin(Math.toRadians(yaw)) * 3.0;
            double lookZ = Math.cos(Math.toRadians(yaw)) * 3.0;

            Vec3 targetPos = mob.position().add(lookX, 0, lookZ);

            // Use native navigation so it smoothly steps over blocks and pathways
            mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, this.speedModifier);
        }
    }
}