package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class UmweltRestGoal extends Goal {
    private final Mob mob;

    public UmweltRestGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob instanceof IUmweltEntity ue) {
            UmweltEngine engine = ue.getUmweltEngine();
            // Only rest if tired AND not currently panicking/fighting
            return engine.getEmotionalEngine().getEnergy() < 0.2f &&
                    engine.getEmotionalEngine().getArousal() < 0.3f;
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        int restTimer = 0;
    }

    @Override
    public void tick() {
        if (mob instanceof IUmweltEntity ue) {
            UmweltEngine engine = ue.getUmweltEngine();

            // Find friends nearby to help recover from the trauma
            AABB recoverZone = mob.getBoundingBox().inflate(5.0);
            long friendsFound = mob.level().getEntitiesOfClass(Mob.class, recoverZone,
                    e -> e instanceof IUmweltEntity && e != mob).size();

            // Recovery is 20% faster per friend nearby (Social Buff)
            float socialMultiplier = 1.0f + (friendsFound * 0.2f);
            float recoveryAmount = 0.005f * socialMultiplier;

            EmotionAPI.setEnergy(engine, engine.getEmotionalEngine().getEnergy() + recoveryAmount);

            // Slowly heal the grief (Valence) if they are resting and safe
            if (engine.getEmotionalEngine().getValence() < 0.0f) {
                EmotionAPI.setValence(engine, 0.001f * socialMultiplier);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (mob instanceof IUmweltEntity ue) {
            // Stop resting if we are fully charged or something scares us
            return ue.getUmweltEngine().getEmotionalEngine().getEnergy() < 0.9f &&
                    ue.getUmweltEngine().getEmotionalEngine().getArousal() < 0.5f;
        }
        return false;
    }
}