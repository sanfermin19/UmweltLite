package com.fermine.umweltlite.engine.personality.engine;

/**
 * The Data Record the appraise method returns.
 */
public record AppraisalResult(
        float threatWeight,
        float curiosityWeight,
        float socialWeight
) {}
