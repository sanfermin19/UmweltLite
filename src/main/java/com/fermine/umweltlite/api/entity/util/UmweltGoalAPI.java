package com.fermine.umweltlite.api.entity.util;

import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.function.Predicate;

/**
 * Allows goals to be gated by internal emotional states (Valence, Arousal, Energy)
 * and individual personality traits (Bravery, Anxiety, Playfulness, etc.).
 * Optimized for real-time state evaluation with zero runtime record allocations.
 */
public class UmweltGoalAPI {

    /**
     * Define a range for internal emotional values.
     */
    public record EmotionalRange(float min, float max) {
        public static EmotionalRange atLeast(float min) { return new EmotionalRange(min, 1.0f); }
        public static EmotionalRange atMost(float max) { return new EmotionalRange(-1.0f, max); }
        public static EmotionalRange any() { return new EmotionalRange(-1.0f, 1.0f); }
    }

    /**
     * Define a requirement for a personality trait.
     */
    public record TraitRange(String trait, float min, float max) {
        public boolean contains(UmweltEngine engine) {
            if (engine == null || "none".equals(trait)) return true;
            float val = PersonalityAPI.getTrait(engine, trait);
            return val >= min && val <= max;
        }

        public static TraitRange atLeast(String trait, float min) { return new TraitRange(trait, min, 1.0f); }
        public static TraitRange atMost(String trait, float max) { return new TraitRange(trait, 0.0f, max); }
        public static TraitRange any() { return new TraitRange("none", -1.0f, 2.0f); }
    }

    /**
     * Injects a goal based off personality and emotional ranges.
     */
    public static void addComplexGoal(Mob mob, int priority, Goal goal,
                                      EmotionalRange v, EmotionalRange a, EmotionalRange e,
                                      TraitRange traitRequirement) {

        // Capture primitive values or stable string references outside the loop execution path
        final float vMin = v.min(), vMax = v.max();
        final float aMin = a.min(), aMax = a.max();
        final float eMin = e.min(), eMax = e.max();
        final String targetTrait = traitRequirement.trait();
        final float tMin = traitRequirement.min(), tMax = traitRequirement.max();
        final boolean skipTrait = "none".equals(targetTrait);

        addEmotionalGoal(mob, priority, goal, engine -> {
            if (engine == null || engine.getEmotionalEngine() == null) return false;

            // Raw comparative floating point checks are lightning fast on the CPU
            float currentV = engine.getEmotionalEngine().getValence();
            if (currentV < vMin || currentV > vMax) return false;

            float currentA = engine.getEmotionalEngine().getArousal();
            if (currentA < aMin || currentA > aMax) return false;

            float currentE = engine.getEmotionalEngine().getEnergy();
            if (currentE < eMin || currentE > eMax) return false;

            if (!skipTrait) {
                float traitVal = PersonalityAPI.getTrait(engine, targetTrait);
                return traitVal >= tMin && traitVal <= tMax;
            }

            return true;
        });
    }

    /**
     * Overload: Injects a goal gated only by Emotion (Any personality).
     */
    public static void addComplexGoal(Mob mob, int priority, Goal goal,
                                      EmotionalRange v, EmotionalRange a, EmotionalRange e) {
        addComplexGoal(mob, priority, goal, v, a, e, TraitRange.any());
    }

    /**
     * Base method for functional predicates.
     */
    public static void addEmotionalGoal(Mob mob, int priority, Goal goal, Predicate<UmweltEngine> condition) {
        mob.goalSelector.addGoal(priority, new WrappedUmweltGoal(goal, mob, condition));
    }

    /**
     * Internal Wrapper to bridge Umwelt logic with the Vanilla GoalSelector.
     * Enforces active state validation on execution loops.
     */
    private static class WrappedUmweltGoal extends Goal {
        private final Goal internal;
        private final Mob mob;
        private final Predicate<UmweltEngine> condition;

        public WrappedUmweltGoal(Goal internal, Mob mob, Predicate<UmweltEngine> condition) {
            this.internal = internal;
            this.mob = mob;
            this.condition = condition;
            this.setFlags(internal.getFlags());
        }

        private boolean checkUmweltContext() {
            if (this.mob instanceof IUmweltEntity ue) {
                UmweltEngine engine = ue.getUmweltEngine();
                return engine != null && this.condition.test(engine);
            }
            return false;
        }

        @Override
        public boolean canUse() {
            return this.checkUmweltContext() && this.internal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.checkUmweltContext() && this.internal.canContinueToUse();
        }

        @Override
        public boolean isInterruptable() {
            return !this.checkUmweltContext() || this.internal.isInterruptable();
        }

        @Override public void start() { this.internal.start(); }
        @Override public void stop() { this.internal.stop(); }
        @Override public void tick() { this.internal.tick(); }
    }
}