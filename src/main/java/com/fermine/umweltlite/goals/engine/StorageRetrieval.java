package com.fermine.umweltlite.goals.engine;

import com.fermine.umweltlite.goals.engine.emotion.map.AttachmentMap;
import com.fermine.umweltlite.goals.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.goals.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.goals.engine.sensory.raycast.RaycastResult;

import java.util.Map;
import java.util.UUID;

/**
 * An immutable snapshot of the internal brain architectures compiled during a single server tick.
 * Read safely by processing nodes and locomotion drivers to isolate action loops from shifting engine states.
 */
public record StorageRetrieval(
        SensoryIntake sensoryIntake,
        Map<UUID, AttachmentMap> socialSnapshot,
        EmotionalMap emotionalState
) {
    /**
     * Convenient utility bridge for extracting the primary tracking ray cast data.
     * Returns RaycastResult. EMPTY or null variants safely depending on your implementation if the token is missing.
     */
    public RaycastResult centerRay() {
        if (this.sensoryIntake == null) {
            return null; // Change to RaycastResult.EMPTY if your system uses an empty object singleton
        }
        return this.sensoryIntake.getRay("center");
    }
}