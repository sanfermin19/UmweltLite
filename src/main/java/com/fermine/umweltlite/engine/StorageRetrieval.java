package com.fermine.umweltlite.engine;


import com.fermine.umweltlite.engine.emotion.map.AttachmentMap;
import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.engine.sensory.raycast.RaycastResult;

import java.util.Map;
import java.util.UUID;

/**
 * An immutable snapshot of the brain's state for a single tick.
 * Used by the BodyEngine and its sub-systems to make movement decisions.
 */
public record StorageRetrieval(
        com.fermine.umweltlite.engine.sensory.engine.SensoryIntake sensoryIntake,
        Map<UUID, AttachmentMap> socialSnapshot,
        EmotionalMap emotionalState
) {
    // Helper to keep existing code working using the new Map-based intake
    public RaycastResult centerRay() {
        return sensoryIntake.getRay("center");
    }
}