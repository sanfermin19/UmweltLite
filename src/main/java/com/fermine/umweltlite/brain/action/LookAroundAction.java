package com.fermine.umweltlite.brain.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LookAroundAction implements IBrainAction {
    private final float deltaYaw;

    public LookAroundAction(float deltaYaw) {
        this.deltaYaw = deltaYaw;
    }

    @Override
    public String getActionId() {
        return "look_around";
    }

    @Override
    public void execute(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            // Fix: Use the mob's direct Y rotation and add the delta offset
            float targetYaw = (float) Math.toRadians(mob.getYRot() + deltaYaw);
            double lookX = -Math.sin(targetYaw) * 2.0;
            double lookZ = Math.cos(targetYaw) * 2.0;

            Vec3 lookTarget = mob.position().add(lookX, mob.getEyeHeight(), lookZ);

            // Native LookControl smoothly interpolates the head turn over several frames
            mob.getLookControl().setLookAt(lookTarget.x, lookTarget.y, lookTarget.z, 10.0F, (float) mob.getMaxHeadXRot());
        }
    }
}