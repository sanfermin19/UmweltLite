package com.fermine.umweltlite.example.action;

import com.fermine.umweltlite.brain.inter.IUmweltAction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;

public class CowGrazeAction implements IUmweltAction {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("umweltlite", "cow_grazing");
    private BlockPos targetGrass = null;
    private int eatTimer = 0;

    @Override
    public ResourceLocation getActionId() { return ID; }

    @Override
    public String getCategory() { return "self_interest"; }

    @Override
    public float calculateUrgency(Mob host, float analytical, float survival, float selfInterest) {
        // Grazing urgency directly reflects biological self-interest pressure (hunger)
        // Highly analytical cows might weigh social spacing, but for now it's pure biological need
        return selfInterest;
    }

    @Override
    public void start(Mob host) {
        this.eatTimer = 0;
        this.targetGrass = null;
    }

    @Override
    public void tick(Mob host) {
        if (this.targetGrass == null || host.level().getBlockState(this.targetGrass).isAir()) {
            // Locate nearby grass blocks dynamically
            BlockPos hostPos = host.blockPosition();
            Iterable<BlockPos> checkRange = BlockPos.betweenClosed(hostPos.offset(-4, -1, -4), hostPos.offset(4, 1, 4));
            for (BlockPos pos : checkRange) {
                if (host.level().getBlockState(pos).is(Blocks.SHORT_GRASS) || host.level().getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
                    this.targetGrass = pos.immutable();
                    break;
                }
            }
        }

        if (this.targetGrass != null) {
            host.getNavigation().moveTo(this.targetGrass.getX(), this.targetGrass.getY(), this.targetGrass.getZ(), 1.0D);
            host.getLookControl().setLookAt(this.targetGrass.getX(), this.targetGrass.getY(), this.targetGrass.getZ(), 30.0F, 30.0F);

            if (host.blockPosition().closerThan(this.targetGrass, 2.0D)) {
                host.getNavigation().stop();
                this.eatTimer++;

                // Play eating visual gestures every few ticks
                if (this.eatTimer % 4 == 0) {
                    host.level().broadcastEntityEvent(host, (byte) 10); // Trigger vanilla eating animation particles
                }

                if (this.eatTimer >= 40) { // Spent 2 seconds grazing
                    if (host.level().getBlockState(this.targetGrass).is(Blocks.SHORT_GRASS)) {
                        host.level().destroyBlock(this.targetGrass, false);
                    } else if (host.level().getBlockState(this.targetGrass).is(Blocks.GRASS_BLOCK)) {
                        host.level().setBlockAndUpdate(this.targetGrass, Blocks.DIRT.defaultBlockState());
                    }
                    this.targetGrass = null;
                    this.eatTimer = 0;
                }
            }
        } else {
            // No grass nearby? Wander casually looking for patch matrices
            if (host.getNavigation().isDone() && host.getRandom().nextFloat() < 0.02F) {
                BlockPos randomOffset = host.blockPosition().offset(host.getRandom().nextInt(8) - 4, 0, host.getRandom().nextInt(8) - 4);
                host.getNavigation().moveTo(randomOffset.getX(), randomOffset.getY(), randomOffset.getZ(), 0.8D);
            }
        }
    }

    @Override
    public void stop(Mob host) {
        host.getNavigation().stop();
        this.targetGrass = null;
    }
}