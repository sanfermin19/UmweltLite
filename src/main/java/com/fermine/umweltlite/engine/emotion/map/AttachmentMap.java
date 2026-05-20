package com.fermine.umweltlite.engine.emotion.map;

/**
 * Represents a social bond between entities.
 * bond: 0.0 (Stranger) to 1.0 (Inseparable/Mate)
 * intensity: How "loud" or active the relationship is in the mob's mind.
 */
public record AttachmentMap(float valence, float intensity, float bond) {

    /**
     * Helper to determine if they qualify as mates based on a threshold.
     */
    public boolean isMate() {
        return this.bond >= 0.8f;
    }
}