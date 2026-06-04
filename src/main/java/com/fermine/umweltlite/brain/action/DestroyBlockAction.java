package com.fermine.umweltlite.brain.action;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

public class DestroyBlockAction implements IBrainAction {
    private final BlockPos targetPos;

    public DestroyBlockAction(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    @Override
    public String getActionId() {
        return "destroy_block";
    }

    @Override
    public void execute(LivingEntity entity) {
        if (!entity.level().isEmptyBlock(targetPos)) {
            // Destroys the block, spawning appropriate block drop particles
            entity.level().destroyBlock(targetPos, true, entity);
        }
    }

    public BlockPos getTargetPos() { return targetPos; }
}