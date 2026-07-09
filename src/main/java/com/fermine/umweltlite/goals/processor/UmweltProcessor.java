package com.fermine.umweltlite.processor;


import com.fermine.umweltlite.goals.engine.StorageInsert;
import com.fermine.umweltlite.goals.engine.StorageRetrieval;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import net.minecraft.world.entity.Mob;

public interface UmweltProcessor {
    StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot);

    /**
     * Execution order — lower runs first. Processors above {@link #EXHAUSTION_CUTOFF}
     * are skipped when the entity is exhausted. Override to place your processor correctly:
     * <ul>
     *   <li>10–20: Observers (pure data reads, no movement)</li>
     *   <li>30–50: Motivators (baseline movement and social drives)</li>
     *   <li>80–100: Overrides (survival instincts that must always run)</li>
     * </ul>
     */
    default int priority() { return 50; }

    /**
     * Processors with priority above this value are skipped when the entity is exhausted.
     * Override {@link #isExhaustionExempt()} to force your processor to always run regardless.
     */
    int EXHAUSTION_CUTOFF = 20;

    /**
     * Return true to guarantee this processor always ticks even when the entity is exhausted.
     * Useful for survival-tier processors that must never be gated.
     */
    default boolean isExhaustionExempt() { return false; }
}