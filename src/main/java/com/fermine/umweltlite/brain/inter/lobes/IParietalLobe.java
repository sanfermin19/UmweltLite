package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.core.BlockPos;

public interface IParietalLobe extends IBrainComponent {
    /**
     * Updates the internal 3D spatial map of the immediate surroundings.
     */
    void updateSpatialMap(BlockPos currentPos);

    float getPainIndex();          // Integrates directional damage into a localized somatic state
    float getThermalComfort();     // Tracks environmental exposure (lava proximity, powder snow, rain)
}