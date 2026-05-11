package com.fermine.umweltlite.api.entity.goal;

import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Survival-focused Goal for UmweltLite.
 * Intensity and duration are dictated by Anxiety and Arousal traits.
 * Now supports custom speed modifiers for behavioral envelopes.
 */
public class UmweltPanicGoal extends Goal {
    private final PathfinderMob mob;
    private final UmweltEngine engine;
    private final double speedModifier; // The multiplier for the behavioral envelope
    private Vec3 panicPos;

    /**
     * Default constructor (Speed modifier = 1.0)
     */
    public UmweltPanicGoal(IUmweltEntity umweltMob) {
        this(umweltMob, 1.0D);
    }

    /**
     * Advanced constructor for fine-tuning panic intensity via UmweltGoalAPI.
     */
    public UmweltPanicGoal(IUmweltEntity umweltMob, double speedModifier) {
        this.mob = (PathfinderMob) umweltMob;
        this.engine = umweltMob.getUmweltEngine();
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 1. Systems Check: Only trigger if Arousal is high (Stress state)
        // This ensures the "Void Pig" (Arousal 0.01) remains paralyzed rather than running
        if (engine.getEmotionalEngine().getArousal() < 0.4f) {
            return false;
        }

        // 2. Trigger: Check if the mob was recently hurt or sees a high-threat entity
        if (mob.getLastHurtByMob() != null || mob.isOnFire()) {
            this.panicPos = DefaultRandomPos.getPos(this.mob, 16, 7);
            return this.panicPos != null;
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // High anxiety mobs will keep running longer even after reaching the first safe spot
        float anxiety = PersonalityAPI.getTrait(engine, "anxiety");
        return !this.mob.getNavigation().isDone() || (mob.getRandom().nextFloat() < anxiety * 0.1f);
    }

    @Override
    public void start() {
        // Apply the calculated panic speed multiplied by our envelope's modifier
        this.mob.getNavigation().moveTo(
                panicPos.x,
                panicPos.y,
                panicPos.z,
                getPanicSpeed() * speedModifier
        );
    }

    @Override
    public void stop() {
        this.panicPos = null;
        this.mob.getNavigation().stop();

        // POST-ADRENALINE CRASH: After panicking, the mob should be exhausted
        // Drop Valence (Scared), Drop Arousal (Calming down), High Energy Cost (Physical strain)
        engine.getEmotionalEngine().modifyState(-0.1f, -0.05f, -0.2f);
    }

    /**
     * Panic speed scales aggressively with Arousal.
     * Maxes out higher than the stroll goal to simulate adrenaline.
     */
    private double getPanicSpeed() {
        float arousal = engine.getEmotionalEngine().getArousal();
        float bravery = PersonalityAPI.getTrait(engine, "bravery");

        // Bravery acts as a stabilizer. High bravery = controlled retreat.
        // Low bravery + High arousal = blind sprinting.
        // Base is 1.2D (faster than standard walking)
        return 1.2D + (arousal * 0.5D) - (bravery * 0.2D);
    }
}