package com.fermine.umweltlite.api.entity.util;

import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.function.Predicate;

/**
 * The Command Center for Umwelt-driven AI.
 * Allows goals to be gated by internal emotional states (Valence, Arousal, Energy)
 * and individual personality traits (Bravery, Anxiety, etc.).
 */
public class UmweltGoalAPI {

    /**
     * Define a range for internal emotional values.
     */
    public record EmotionalRange(float min, float max) {
        public boolean contains(float value) { return value >= min && value <= max; }

        public static EmotionalRange atLeast(float min) { return new EmotionalRange(min, 1.0f); }
        public static EmotionalRange atMost(float max) { return new EmotionalRange(-1.0f, max); }
        public static EmotionalRange any() { return new EmotionalRange(-1.0f, 1.0f); }
    }

    /**
     * Define a requirement for a personality trait.
     */
    public record TraitRange(String trait, float min, float max) {
        public boolean contains(UmweltEngine engine) {
            if (trait.equals("none")) return true;
            float val = PersonalityAPI.getTrait(engine, trait);
            return val >= min && val <= max;
        }

        // Matching the EmotionalRange syntax for consistency
        public static TraitRange atLeast(String trait, float min) { return new TraitRange(trait, min, 1.0f); }
        public static TraitRange atMost(String trait, float max) { return new TraitRange(trait, 0.0f, max); }
        public static TraitRange any() { return new TraitRange("none", -1.0f, 2.0f); }
    }

    /**
     * The Master Method: Injects a goal gated by both Emotion and Personality.
     */
    public static void addComplexGoal(Mob mob, int priority, Goal goal,
                                      EmotionalRange v, EmotionalRange a, EmotionalRange e,
                                      TraitRange traitRequirement) {

        addEmotionalGoal(mob, priority, goal, engine ->
                v.contains(engine.getEmotionalEngine().getValence()) &&
                        a.contains(engine.getEmotionalEngine().getArousal()) &&
                        e.contains(engine.getEmotionalEngine().getEnergy()) &&
                        (traitRequirement.trait().equals("none") || traitRequirement.contains(engine))
        );
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
     */
    private static class WrappedUmweltGoal extends Goal {
        private final Goal internal;
        private final Mob mob;
        private final Predicate<UmweltEngine> condition;

        public WrappedUmweltGoal(Goal internal, Mob mob, Predicate<UmweltEngine> condition) {
            this.internal = internal;
            this.mob = mob;
            this.condition = condition;
        }

        @Override
        public boolean canUse() {
            if (mob instanceof com.fermine.umweltlite.api.entity.IUmweltEntity ue) {
                return condition.test(ue.getUmweltEngine()) && internal.canUse();
            }
            return false;
        }

        @Override public boolean canContinueToUse() { return internal.canContinueToUse(); }
        @Override public boolean isInterruptable() { return internal.isInterruptable(); }
        @Override public void start() { internal.start(); }
        @Override public void stop() { internal.stop(); }
        @Override public void tick() { internal.tick(); }
    }
}