package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.world.entity.Entity;
import java.util.List;

public interface IOccipitalLobe extends IBrainComponent {
    /**
     * Processes raw visual data (like raycast results) into categorized objects.
     */
    void processVisualField(List<Entity> visibleEntities);

    float getVisualAcuity();       // Affected by blindness, light levels, or fatigue
    boolean isTargetInFocus();     // True if tracking a specific entity intensely
}