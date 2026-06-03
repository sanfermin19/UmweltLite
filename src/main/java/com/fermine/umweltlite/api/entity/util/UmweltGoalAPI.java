package com.fermine.umweltlite.api.entity.util;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.function.Predicate;

/**
 * Allows goals to be gated by internal emotional states (Valence, Arousal, Energy),
 * individual personality traits, semantic knowledge thresholds, AND episodic memory criteria.
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
     * Defines a requirement matching an internal confidence threshold metric for a precise memory fact.
     */
    public record KnowledgeThreshold(String factKey, float minConfidence, long halfLife) {
        public boolean matches(UmweltEngine engine) {
            if (engine == null || "none".equals(factKey)) return true;
            float currentConf = KnowledgeAPI.getFactConfidence(engine, factKey, halfLife);
            return currentConf >= minConfidence;
        }

        public static KnowledgeThreshold masterThreshold(String key, float minConfidence, long halfLife) {
            return new KnowledgeThreshold(key, minConfidence, halfLife);
        }

        public static KnowledgeThreshold any() {
            return new KnowledgeThreshold("none", 0.0f, 1L);
        }
    }

    /**
     * Gating filter checking for recent episodic occurrences inside the short-term memory buffer.
     * Offers context-matching criteria alongside intensity verification layers.
     */
    public record MemoryRequirement(String contextKey, String contextValue, boolean requireSignificant, float minRetention) {
        public boolean matches(UmweltEngine engine) {
            if (engine == null || "none".equals(contextKey)) return true;

            // Stream directly through the encapsulated collection exposed by our MemoryAPI layer
            return engine.getMemoryEngine().getShortTermMemories().stream().anyMatch(m -> {
                if (!m.matchesContext(contextKey, contextValue)) return false;
                if (requireSignificant && !m.isSignificant()) return false;

                float strength = m.getRetention(engine.getMob().level().getGameTime(), 24000L);
                return strength >= minRetention;
            });
        }

        public static MemoryRequirement matchingTag(String key, String value) {
            return new MemoryRequirement(key, value, false, 0.0f);
        }

        public static MemoryRequirement freshTrauma(String key, String value) {
            return new MemoryRequirement(key, value, true, 0.5f);
        }

        public static MemoryRequirement any() {
            return new MemoryRequirement("none", "none", false, 0.0f);
        }
    }

    /**
     * Injects a comprehensive cognitive goal gated by emotional bounds, trait values, semantic knowledge, AND short-term memory flags.
     */
    public static void addCognitiveGoal(Mob mob, int priority, Goal goal,
                                        EmotionalRange v, EmotionalRange a, EmotionalRange e,
                                        TraitRange traitRequirement, KnowledgeThreshold knowledgeRequirement,
                                        MemoryRequirement memoryRequirement) {

        // Cache primitives outside execution paths to ensure flat performance inside intense tick execution loops
        final float vMin = v.min(), vMax = v.max();
        final float aMin = a.min(), aMax = a.max();
        final float eMin = e.min(), eMax = e.max();

        final String targetTrait = traitRequirement.trait();
        final float tMin = traitRequirement.min(), tMax = traitRequirement.max();
        final boolean skipTrait = "none".equals(targetTrait);

        final String targetFact = knowledgeRequirement.factKey();
        final float kMinConfidence = knowledgeRequirement.minConfidence();
        final long kHalfLife = knowledgeRequirement.halfLife();
        final boolean skipKnowledge = "none".equals(targetFact);

        final String mKey = memoryRequirement.contextKey();
        final String mVal = memoryRequirement.contextValue();
        final boolean mSig = memoryRequirement.requireSignificant();
        final float mMinRet = memoryRequirement.minRetention();
        final boolean skipMemory = "none".equals(mKey);

        addEmotionalGoal(mob, priority, goal, engine -> {
            if (engine == null || engine.getEmotionalEngine() == null || engine.getKnowledgeEngine() == null || engine.getMemoryEngine() == null) return false;

            // 1. Core Circumplex Comparative Sweep
            float currentV = engine.getEmotionalEngine().getValence();
            if (currentV < vMin || currentV > vMax) return false;

            float currentA = engine.getEmotionalEngine().getArousal();
            if (currentA < aMin || currentA > aMax) return false;

            float currentE = engine.getEmotionalEngine().getEnergy();
            if (currentE < eMin || currentE > eMax) return false;

            // 2. Personality Archetype Verification
            if (!skipTrait) {
                float traitVal = PersonalityAPI.getTrait(engine, targetTrait);
                if (traitVal < tMin || traitVal > tMax) return false;
            }

            // 3. Epistemological Gating Sweep ("Knowing too much or just enough")
            if (!skipKnowledge) {
                float currentConf = KnowledgeAPI.getFactConfidence(engine, targetFact, kHalfLife);
                if (currentConf < kMinConfidence) return false;
            }

            // 4. Episodic Buffer Scan
            if (!skipMemory) {
                long time = engine.getMob().level().getGameTime();
                return engine.getMemoryEngine().getShortTermMemories().stream().anyMatch(m -> {
                    if (!m.matchesContext(mKey, mVal)) return false;
                    if (mSig && !m.isSignificant()) return false;
                    return m.getRetention(time, 24000L) >= mMinRet;
                });
            }

            return true;
        });
    }

    /**
     * Overload: Injects a cognitive goal using standard memory parameters, bypassing specialized episodic checks.
     */
    public static void addCognitiveGoal(Mob mob, int priority, Goal goal,
                                        EmotionalRange v, EmotionalRange a, EmotionalRange e,
                                        TraitRange traitRequirement, KnowledgeThreshold knowledgeRequirement) {
        addCognitiveGoal(mob, priority, goal, v, a, e, traitRequirement, knowledgeRequirement, MemoryRequirement.any());
    }

    /**
     * Overload: Injects a legacy complex goal based off personality and emotional ranges (ignoring knowledge/episodic variables).
     */
    public static void addComplexGoal(Mob mob, int priority, Goal goal,
                                      EmotionalRange v, EmotionalRange a, EmotionalRange e,
                                      TraitRange traitRequirement) {
        addCognitiveGoal(mob, priority, goal, v, a, e, traitRequirement, KnowledgeThreshold.any(), MemoryRequirement.any());
    }

    /**
     * Overload: Injected goal gated exclusively via emotion metrics (ignoring personality profiles, knowledge, and episodic parameters).
     */
    public static void addComplexGoal(Mob mob, int priority, Goal goal,
                                      EmotionalRange v, EmotionalRange a, EmotionalRange e) {
        addCognitiveGoal(mob, priority, goal, v, a, e, TraitRange.any(), KnowledgeThreshold.any(), MemoryRequirement.any());
    }

    /**
     * Base implementation method supplying functional structural custom predicate mapping patterns.
     */
    public static void addEmotionalGoal(Mob mob, int priority, Goal goal, Predicate<UmweltEngine> condition) {
        if (mob == null || goal == null || condition == null) return;
        mob.goalSelector.addGoal(priority, new WrappedUmweltGoal(goal, mob, condition));
    }

    /**
     * Internal Wrapper designed to bridge complex environmental logic matrices safely with the Vanilla GoalSelector engine.
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