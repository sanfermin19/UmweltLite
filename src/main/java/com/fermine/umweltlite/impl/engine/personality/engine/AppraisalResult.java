package com.fermine.umweltlite.impl.engine.personality.engine;

/**
 * The processed emotional value output generated during environmental appraisal scans.
 * Values are strictly clamped between 0.0f and 1.0f.
 */
public record AppraisalResult(
        float threatWeight,
        float curiosityWeight,
        float socialWeight
) {
    public static final AppraisalResult CALM = new AppraisalResult(0.0f, 0.0f, 0.0f);
}