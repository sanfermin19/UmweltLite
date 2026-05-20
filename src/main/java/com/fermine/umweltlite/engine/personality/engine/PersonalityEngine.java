package com.fermine.umweltlite.engine.personality.engine;

import com.fermine.umweltlite.engine.sensory.engine.SensoryIntake;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;

public class PersonalityEngine {
    private final long personalitySeed;
    private final Map<String, Float> activeTraits = new HashMap<>();
    private final Map<String, Float> overriddenTraits = new HashMap<>(); // Player-injected overrides

    public PersonalityEngine(long seed) {
        this.personalitySeed = seed;
        this.generateFeralTraits();
    }

    public void applyTemplate(PersonalityTemplate template) {
        RandomSource random = RandomSource.create(this.personalitySeed);

        template.baseTraits().forEach((trait, baseValue) -> {
            float offset = (random.nextFloat() - 0.5f) * template.variance();
            activeTraits.put(trait, Mth.clamp(baseValue + offset, 0f, 1f));
        });
    }

    /**
     * Injects a specific trait value that ignores the DNA seed and templates.
     * Set to null to remove the override and return to "natural" behavior.
     */
    public void setOverride(String trait, Float value) {
        if (value == null) {
            overriddenTraits.remove(trait);
        } else {
            overriddenTraits.put(trait, Mth.clamp(value, 0f, 1f));
        }
    }

    private void generateFeralTraits() {
        RandomSource random = RandomSource.create(this.personalitySeed);
        activeTraits.put("bravery", random.nextFloat());
        activeTraits.put("empathy", random.nextFloat());
        activeTraits.put("anxiety", random.nextFloat());
        activeTraits.put("playfulness", random.nextFloat());
    }

    public float getTrait(String key) {
        // Hierarchy: Override -> Natural Trait -> Default
        if (overriddenTraits.containsKey(key)) {
            return overriddenTraits.get(key);
        }
        return activeTraits.getOrDefault(key, 0.5f);
    }

    public AppraisalResult appraise(SensoryIntake intake) {
        var ray = intake.getRay("center");

        if (ray == null || !ray.isBlocked()) {
            return new AppraisalResult(0f, 0f, 0f);
        }

        float threat = 0f;
        float curiosity;
        float social = 0f;

        if (ray.isEntity()) {
            // High anxiety + Low bravery = High neuroticism
            float neuroticism = (getTrait("anxiety") - getTrait("bravery") + 1.0f) / 2.0f;
            threat = 0.3f + (neuroticism * 0.4f);
            social = getTrait("empathy");
            curiosity = getTrait("playfulness") * 0.5f;
        } else {
            curiosity = getTrait("playfulness") * 0.8f;
            // Scared of the dark if anxiety is high
            if (intake.worldState.lightLevel() < 0.2f) {
                threat = getTrait("anxiety") * 0.5f;
            }
        }

        return new AppraisalResult(
                Mth.clamp(threat, 0f, 1f),
                Mth.clamp(curiosity, 0f, 1f),
                Mth.clamp(social, 0f, 1f)
        );
    }

    // Ensure overrides are saved
    public Map<String, Float> getOverrides() {
        return overriddenTraits;
    }

    public Map<String, Float> getActiveTraits() {
        // Start with a copy of the natural/template traits
        Map<String, Float> merged = new HashMap<>(this.activeTraits);

        // Overlay the player-injected overrides
        merged.putAll(this.overriddenTraits);

        return merged;
    }
}