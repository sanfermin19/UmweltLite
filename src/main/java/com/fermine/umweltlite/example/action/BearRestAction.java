package com.fermine.umweltlite.example.action;

import com.fermine.umweltlite.brain.inter.IUmweltAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class BearRestAction implements IUmweltAction {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("umweltlite", "bear_resting");

    @Override
    public ResourceLocation getActionId() { return ID; }

    @Override
    public String getCategory() { return "self_interest"; }

    @Override
    public float calculateUrgency(Mob host, float analytical, float survival, float selfInterest) {
        // Resting becomes highly critical when self-interest biological fatigue scales up
        return selfInterest;
    }

    @Override
    public void tick(Mob host) {
        host.getNavigation().stop();
    }
}