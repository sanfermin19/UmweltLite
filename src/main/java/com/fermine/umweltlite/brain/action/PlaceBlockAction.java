package com.fermine.umweltlite.brain.action;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceBlockAction implements IBrainAction {
    private final BlockPos targetPos;
    private final BlockState blockState;

    // Fix: Accept both the position AND the specific BlockState to place down
    public PlaceBlockAction(BlockPos targetPos, BlockState blockState) {
        this.targetPos = targetPos;
        this.blockState = blockState;
    }

    // Overload Constructor: Fallback default (e.g., placing grass blocks for nesting/grazing marks)
    public PlaceBlockAction(BlockPos targetPos) {
        this.targetPos = targetPos;
        this.blockState = Blocks.GRASS_BLOCK.defaultBlockState();
    }

    @Override
    public String getActionId() {
        return "place_block";
    }

    @Override
    public void execute(LivingEntity entity) {
        // Safe validation checks to make sure the position is in a loaded chunk and is empty
        if (this.targetPos != null && this.blockState != null && entity.level().isEmptyBlock(this.targetPos)) {
            entity.level().setBlock(this.targetPos, this.blockState, 3);
        }
    }

    public BlockPos getTargetPos() { return this.targetPos; }
    public BlockState getBlockState() { return this.blockState; }
}