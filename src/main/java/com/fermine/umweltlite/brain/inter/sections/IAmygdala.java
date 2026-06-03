package com.fermine.umweltlite.brain.inter.sections;

import com.fermine.umweltlite.brain.inter.IBrainComponent;

public interface IAmygdala extends IBrainComponent {
    float getPanicFloater();      // 0.0 (Calm) to 1.0 (Absolute Terror)
    float getAggressionFloater();  // 0.0 (Passive) to 1.0 (Enraged)

    /**
     * Triggers an immediate spike in adrenaline/fear based on an external shock.
     */
    void triggerShock(float intensity);
}