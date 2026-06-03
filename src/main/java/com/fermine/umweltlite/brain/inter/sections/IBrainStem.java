package com.fermine.umweltlite.brain.inter.sections;

import com.fermine.umweltlite.brain.inter.IBrainComponent;

public interface IBrainStem extends IBrainComponent {
    float getHeartRate();       // Beats per minute simulation
    float getExhaustion();      // Metabolic drain

    boolean isSuffocating();
    void exertEnergy(float amount);
}