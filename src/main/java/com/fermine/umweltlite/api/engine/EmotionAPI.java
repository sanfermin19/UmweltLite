package com.fermine.umweltlite.api.engine;

import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.emotion.map.AttachmentMap;
import com.fermine.umweltlite.utils.UmweltNBTUtils;

import java.util.Optional;
import java.util.UUID;

public class EmotionAPI {

    // --- Core Circumplex Scalar State Mutators ---

    /**
     * Sets the energy axis to a specific value by calculating the necessary state delta.
     * Values are bounded downstream within the core emotional engine matrix.
     */
    public static void setEnergy(UmweltEngine engine, float targetValue) {
        if (engine == null) return;
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 1.0f);
        float delta = safeTarget - ee.getEnergy();
        ee.modifyState(0.0f, 0.0f, delta);
    }

    /**
     * Sets the valence axis to a specific value by calculating the necessary state delta.
     * Values are bounded downstream within the core emotional engine matrix.
     */
    public static void setValence(UmweltEngine engine, float targetValue) {
        if (engine == null) return;
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 0.0f);
        float delta = safeTarget - ee.getValence();
        ee.modifyState(delta, 0.0f, 0.0f);
    }

    /**
     * Sets the arousal axis to a specific value by calculating the necessary state delta.
     * Values are bounded downstream within the core emotional engine matrix.
     */
    public static void setArousal(UmweltEngine engine, float targetValue) {
        if (engine == null) return;
        var ee = engine.getEmotionalEngine();
        float safeTarget = UmweltNBTUtils.safeFloat(targetValue, 0.0f);
        float delta = safeTarget - ee.getArousal();
        ee.modifyState(0.0f, delta, 0.0f);
    }

    /**
     * Directly forces a specific emotional state configuration across all three circumplex axes simultaneously.
     */
    public static void setEmotionalState(UmweltEngine engine, float v, float a, float e) {
        if (engine == null) return;
        setValence(engine, v);
        setArousal(engine, a);
        setEnergy(engine, e);
    }

    // --- Social Attachment & Relationship API Hooks ---

    /**
     * Injects or updates a structured social relationship map for a specific target entity UUID.
     * Use this when a mob interacts socially, takes damage from an ally, or undergoes relationship shifts.
     */
    public static void setSocialAttachment(UmweltEngine engine, UUID targetUUID, AttachmentMap map) {
        if (engine == null || targetUUID == null || map == null) return;
        engine.getEmotionalEngine().setSocialAttachment(targetUUID, map);
    }

    /**
     * Securely retrieves an active relationship profile map for a target entity UUID if it exists.
     */
    public static Optional<AttachmentMap> getSocialAttachment(UmweltEngine engine, UUID targetUUID) {
        if (engine == null || targetUUID == null) return Optional.empty();
        return Optional.ofNullable(engine.getEmotionalEngine().getSocialSnapshot().get(targetUUID));
    }

    /**
     * Removes an active relationship entry completely from the mob's social memory workspace.
     * Ideal for clearing dead targets, executing memory wipe mechanics, or forcing amnesia profiles.
     */
    public static void clearSocialAttachment(UmweltEngine engine, UUID targetUUID) {
        if (engine == null || targetUUID == null) return;
        engine.getEmotionalEngine().clearSocialAttachment(targetUUID);
    }

    /**
     * Evaluates whether the tracked social bond configuration qualifies two specific entities
     * to bypass generic vanilla behavior limits and function as deep domestic or tribal partners.
     */
    public static boolean isMate(UmweltEngine engine, UUID targetUUID) {
        if (engine == null || targetUUID == null) return false;
        return getSocialAttachment(engine, targetUUID)
                .map(AttachmentMap::isMate)
                .orElse(false);
    }

    /**
     * Shifts relationship parameters relatively without overwriting historical tracking values completely.
     */
    public static void modifySocialAttachment(UmweltEngine engine, UUID targetUUID, float vDelta, float iDelta, float bDelta) {
        if (engine == null || targetUUID == null) return;

        AttachmentMap current = engine.getEmotionalEngine().getSocialSnapshot()
                .getOrDefault(targetUUID, new AttachmentMap(0.0f, 0.0f, 0.0f));

        AttachmentMap modified = new AttachmentMap(
                current.valence() + vDelta,
                current.arousal() + iDelta,
                current.bond() + bDelta
        );

        engine.getEmotionalEngine().setSocialAttachment(targetUUID, modified);
    }
}