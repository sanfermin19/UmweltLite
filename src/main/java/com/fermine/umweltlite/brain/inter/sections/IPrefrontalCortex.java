package com.fermine.umweltlite.brain.inter.sections;

import com.fermine.umweltlite.brain.inter.IBrainComponent;

public interface IPrefrontalCortex extends IBrainComponent {
    /**
     * Calculates the current dominant drive of the organism.
     * * @return A string identifier or enum representing the active intent (e.g., "REST", "FLEE", "FORAGE").
     */
    String determineCurrentWill();

    float getDecisionFatigue(); // High values make choice selection sluggish or erratic
}