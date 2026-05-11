package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class UmweltAttackGoal extends MeleeAttackGoal {
    private final PathfinderMob mob;
    private final double baseSpeed; // Storing our own copy to bypass private access
    private int ticksSinceLastAction = 0;

    public UmweltAttackGoal(PathfinderMob mob, double speed, boolean followingTargetEvenIfNotSeen) {
        super(mob, speed, followingTargetEvenIfNotSeen);
        this.mob = mob;
        this.baseSpeed = speed;
    }

    @Override
    public boolean canUse() {
        if (mob instanceof IUmweltEntity ue) {
            // Check API for energy before committing to the bit
            if (ue.getUmweltEngine().getEmotionalEngine().getEnergy() < 0.15f) {
                return false;
            }
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (mob instanceof IUmweltEntity ue) {
            UmweltEngine engine = ue.getUmweltEngine();
            float energy = engine.getEmotionalEngine().getEnergy();
            float arousal = engine.getEmotionalEngine().getArousal();

            // 1. PHYSICAL EXHAUSTION
            if (energy <= 0.02f) return false;

            // 2. LOSS OF INTEREST
            if (arousal < 0.1f) return false;
        }
        return super.canContinueToUse();
    }

    @Override
    public void tick() {
        super.tick();
        ticksSinceLastAction++;

        if (mob instanceof IUmweltEntity ue) {
            UmweltEngine engine = ue.getUmweltEngine();
            float v = engine.getEmotionalEngine().getValence();
            float a = engine.getEmotionalEngine().getArousal();
            float currentEnergy = engine.getEmotionalEngine().getEnergy();

            // --- THE "BERSERK" LOGIC ---
            // Using baseSpeed since we can't touch super.speedModifier
            if (a > 0.85f) {
                this.mob.setSpeed((float) (this.baseSpeed * 1.2f));
            } else {
                this.mob.setSpeed((float) this.baseSpeed);
            }

            // --- THE "SADIST" LOGIC ---
            if (v > 0.5f && ticksSinceLastAction % 40 == 0) {
                if (mob.onGround()) {
                    this.mob.getJumpControl().jump();
                    this.ticksSinceLastAction = 0;
                }
            }

            // --- METABOLIC DRAIN ---
            // Using your EmotionAPI method
            if (mob.getNavigation().isInProgress()) {
                EmotionAPI.setEnergy(engine, currentEnergy - 0.001f);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        // Reset speed to normally using our stored baseSpeed
        this.mob.setSpeed((float) this.baseSpeed);
    }
}