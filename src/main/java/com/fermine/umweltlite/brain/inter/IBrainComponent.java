package com.fermine.umweltlite.brain.inter;

import net.minecraft.world.entity.LivingEntity;

public interface IBrainComponent {
    /**
     * Ticks this specific brain region, allowing it to process inputs
     * from the environment and update its internal neurological states.
     *
     * @param host The physical body this brain is currently inhabiting.
     */
    void tick(LivingEntity host);

    /**
     * Resets this region's floating variables back to a baseline state.
     * Useful for recovering from extreme trauma or resets.
     */
    void reset();
}