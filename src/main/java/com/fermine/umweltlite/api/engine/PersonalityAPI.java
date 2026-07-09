package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.personality.engine.PersonalityTemplate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Global API gateway for reading and manipulating entity personality states.
 * Thread-safe registry handling for NeoForge modding setups.
 */
public class PersonalityAPI {
    private static final Map<String, PersonalityTemplate> REGISTRY = new ConcurrentHashMap<>();

    public static void register(PersonalityTemplate template) {
        if (template != null && template.id() != null) {
            REGISTRY.put(template.id(), template);
        }
    }

    public static PersonalityTemplate getTemplate(String templateId) {
        return REGISTRY.get(templateId);
    }

    /**
     * Gets an active trait value (0.0 to 1.0).
     * Follows strict hierarchy: Override -> Template/Natural Seed -> Default (0.5).
     */
    public static float getTrait(UmweltEngine engine, String trait) {
        if (engine == null || engine.getPersonality() == null) return 0.5f;
        return engine.getPersonality().getTrait(trait);
    }

    /**
     * Helper to verify if a mob possesses sufficient baseline empathy to act on social behaviors.
     */
    public static boolean isSocial(UmweltEngine engine) {
        return getTrait(engine, "empathy") > 0.4f;
    }

    /**
     * Applies a predefined template to the engine, blending its baselines with the mob's unique seed.
     */
    public static void applyTemplate(UmweltEngine engine, String templateId) {
        if (engine == null || engine.getPersonality() == null) return;
        PersonalityTemplate template = REGISTRY.get(templateId);
        if (template != null) {
            engine.getPersonality().applyTemplate(template);
        }
    }

    /**
     * Injects an immutable override for a single trait, completely short-circuiting template or seed data.
     * Expects a value clamped between 0.0f and 1.0f.
     */
    public static void overrideTrait(UmweltEngine engine, String trait, float value) {
        if (engine == null || engine.getPersonality() == null) return;
        engine.getPersonality().setOverride(trait, value);
    }

    /**
     * Clears an existing override, allowing the underlying natural or template-driven traits to express themselves again.
     */
    public static void clearOverride(UmweltEngine engine, String trait) {
        if (engine == null || engine.getPersonality() == null) return;
        engine.getPersonality().setOverride(trait, null);
    }
}