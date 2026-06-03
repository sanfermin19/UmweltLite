package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

public class UmweltRestGoal extends Goal {
    private final Mob mob;
    private int restTimer;
    private float cachedSocialMultiplier = 1.0f;

    public UmweltRestGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (mob instanceof IUmweltEntity ue) {
            UmweltEngine engine = ue.getUmweltEngine();
            return engine.getEmotionalEngine().getEnergy() < 0.2f &&
                    engine.getEmotionalEngine().getArousal() < 0.3f;
        }
        return false;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.restTimer = 0;
        this.cachedSocialMultiplier = 1.0f;
    }

    @Override
    public void tick() {
        if (!(mob instanceof IUmweltEntity ue)) return;
        UmweltEngine engine = ue.getUmweltEngine();

        // THROTTLE THE AABB SEARCH! Only run every 40 ticks (2 seconds).
        if (this.restTimer % 40 == 0) {
            AABB recoverZone = mob.getBoundingBox().inflate(5.0);
            long friendsFound = mob.level().getEntitiesOfClass(Mob.class, recoverZone,
                    e -> e instanceof IUmweltEntity && e != mob).size();
            this.cachedSocialMultiplier = 1.0f + (friendsFound * 0.2f);
        }

        float recoveryAmount = 0.005f * this.cachedSocialMultiplier;
        EmotionAPI.setEnergy(engine, engine.getEmotionalEngine().getEnergy() + recoveryAmount);

        // Fixed: Use modifyState or add, don't use 'setValence' to 0.001
        if (engine.getEmotionalEngine().getValence() < 0.0f) {
            // Assuming modifyState is (valence, arousal, energy)
            engine.getEmotionalEngine().modifyState(0.001f * this.cachedSocialMultiplier, -0.001f, 0);
        }

        this.restTimer++;
    }

    @Override
    public boolean canContinueToUse() {
        if (mob instanceof IUmweltEntity ue) {
            // Stop if energy is full OR if we get scared (arousal spikes)
            return ue.getUmweltEngine().getEmotionalEngine().getEnergy() < 0.9f &&
                    ue.getUmweltEngine().getEmotionalEngine().getArousal() < 0.5f;
        }
        return false;
    }
}