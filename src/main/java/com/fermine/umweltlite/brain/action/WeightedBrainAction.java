package com.fermine.umweltlite.brain.action;

import net.minecraft.world.entity.LivingEntity;

public class WeightedBrainAction {
    private final IBrainAction action;
    private final float weight; // How intensely the brain wants to do this right now (e.g., 0.0 to 1.0+)

    public WeightedBrainAction(IBrainAction action, float weight) {
        this.action = action;
        this.weight = weight;
    }

    public IBrainAction getAction() { return action; }
    public float getWeight() { return weight; }
}