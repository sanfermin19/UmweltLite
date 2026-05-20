package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.personality.engine.PersonalityTemplate;

import java.util.HashMap;
import java.util.Map;

public class PersonalityAPI {
    private static final Map<String, PersonalityTemplate> REGISTRY = new HashMap<>();

    public static void register(PersonalityTemplate template) {
        REGISTRY.put(template.id(), template);
    }

    /**
     * Gets a trait value (0.0 to 1.0).
     * Uses the hierarchy: Override -> Natural Trait -> Default (0.5).
     */
    public static float getTrait(UmweltEngine engine, String trait) {
        return engine.getPersonality().getTrait(trait);
    }

    /**
     * Helper to check if a mob is socially inclined based on its empathy.
     */
    public static boolean isSocial(UmweltEngine engine) {
        return getTrait(engine, "empathy") > 0.4f;
    }

    /**
     * Applies a broad template (blended with the mob's unique seed).
     */
    public static void applyTemplate(UmweltEngine engine, String templateId) {
        PersonalityTemplate template = REGISTRY.get(templateId);
        if (template != null) {
            engine.getPersonality().applyTemplate(template);
        }
    }

    /**
     * Direct injection: Overrides a single trait regardless of template or seed.
     * Use 0.0 to 1.0.
     */
    public static void overrideTrait(UmweltEngine engine, String trait, float value) {
        engine.getPersonality().setOverride(trait, value);
    }

    /**
     * Clears an override to let the mob's "natural" personality take back over.
     */
    public static void clearOverride(UmweltEngine engine, String trait) {
        engine.getPersonality().setOverride(trait, null);
    }
}