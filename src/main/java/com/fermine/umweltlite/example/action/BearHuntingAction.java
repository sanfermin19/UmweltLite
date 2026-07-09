package com.fermine.umweltlite.example.action;

import com.fermine.umweltlite.brain.inter.IUmweltAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.PolarBear;

public class BearHuntingAction implements IUmweltAction {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("umweltlite", "bear_hunting");
    private final LivingEntity target;

    public BearHuntingAction(LivingEntity target) {
        this.target = target;
    }

    @Override
    public ResourceLocation getActionId() { return ID; }

    @Override
    public String getCategory() { return "survival"; }

    @Override
    public float calculateUrgency(Mob host, float analytical, float survival, float selfInterest) {
        if (target == null || !target.isAlive()) return 0.0F;
        // Hunting urgency scales directly with Survival pressure (hunger/adrenaline modifier)
        return survival * 1.2F;
    }

    @Override
    public void tick(Mob host) {
        if (target == null || !target.isAlive()) return;

        host.getNavigation().moveTo(target, 1.4D);
        host.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (host.distanceToSqr(target) <= 3.0D) {
            host.doHurtTarget(target);
            if (host instanceof PolarBear bear) {
                bear.setStanding(true);
            }
        }
    }

    @Override
    public void stop(Mob host) {
        if (host instanceof PolarBear bear) {
            bear.setStanding(false);
        }
        host.getNavigation().stop();
    }
}