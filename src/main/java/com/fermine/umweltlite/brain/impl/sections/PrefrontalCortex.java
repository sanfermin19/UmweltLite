package com.fermine.umweltlite.brain.impl.sections;

import com.fermine.umweltlite.brain.inter.sections.IPrefrontalCortex;
import com.fermine.umweltlite.brain.inter.sections.IAmygdala;
import com.fermine.umweltlite.brain.inter.sections.IBrainStem;
import net.minecraft.world.entity.LivingEntity;

public class PrefrontalCortex implements IPrefrontalCortex {

    private final IAmygdala amygdala;
    private final IBrainStem brainStem;

    private float decisionFatigue = 0.0f; // 0.0 (Sharp) to 1.0 (Completely Burned Out)
    private String currentWill = "IDLE";

    public PrefrontalCortex(IAmygdala amygdala, IBrainStem brainStem) {
        this.amygdala = amygdala;
        this.brainStem = brainStem;
    }

    @Override
    public void tick(LivingEntity host) {
        // Decision fatigue naturally bleeds off if the animal is calm and resting
        if (amygdala.getPanicFloater() < 0.2f) {
            this.decisionFatigue = Math.max(0.0f, this.decisionFatigue - 0.002f);
        } else {
            // Rapidly build up fatigue if constantly under fire or terrified
            this.decisionFatigue = Math.min(1.0f, this.decisionFatigue + 0.005f);
        }

        // Dynamically compute the overarching Will/Intent State based on core biological pressures
        this.currentWill = evaluateInternalWill();
    }

    private String evaluateInternalWill() {
        if (this.amygdala.getPanicFloater() > 0.6f) {
            return "SURVIVAL_FLIGHT";
        }
        if (this.brainStem.getExhaustion() > 0.7f) {
            return "METABOLIC_FORAGE";
        }
        if (this.amygdala.getAggressionFloater() > 0.5f) {
            return "COMBAT_TERRITORIAL";
        }
        return "IDLE_EXPLORE";
    }

    @Override
    public String determineCurrentWill() {
        return this.currentWill;
    }

    @Override
    public float getDecisionFatigue() {
        return this.decisionFatigue;
    }

    @Override
    public void reset() {
        this.decisionFatigue = 0.0f;
        this.currentWill = "IDLE";
    }
}