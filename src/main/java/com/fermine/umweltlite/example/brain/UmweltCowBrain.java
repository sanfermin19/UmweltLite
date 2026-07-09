package com.fermine.umweltlite.example.brain;

import com.fermine.umweltlite.brain.capability.UmweltBrainRegistry;
import com.fermine.umweltlite.brain.impl.UmweltBrainEngine;
import com.fermine.umweltlite.example.action.CowGrazeAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import java.util.List;

public class UmweltCowBrain extends UmweltBrainEngine {

    private boolean predatorNearby = false;
    private float hunger = 0.2F;

    @Override
    public ResourceLocation getBrainTypeId() {
        return UmweltBrainRegistry.COW_BRAIN;
    }

    @Override
    public void perceive(Mob host) {
        // Check a 12-block boundary box radius for active predators
        List<Wolf> predators = host.level().getEntitiesOfClass(Wolf.class, host.getBoundingBox().inflate(12.0D));
        this.predatorNearby = !predators.isEmpty();
    }

    @Override
    protected void processEmotions(Mob host) {
        // Slow continuous internal metabolic breakdown
        this.hunger = Math.min(1.0F, this.hunger + 0.001F);

        if (this.predatorNearby) {
            this.setArousal(Math.min(1.0F, this.getArousal() + 0.15F));  // Adrenaline spike
            this.setValence(Math.max(-1.0F, this.getValence() - 0.1F)); // Fear / Distress
        } else {
            this.setArousal(Math.max(-0.5F, this.getArousal() - 0.02F)); // Calm relaxation
            this.setValence(Math.min(0.5F, this.getValence() + 0.01F));   // Contentment
        }

        // Lower hunger state while grazing action is actively tracking
        if (this.getActiveStageName().equals("umweltlite:cow_grazing")) {
            this.hunger = Math.max(0.0F, this.hunger - 0.008F);
        }
    }

    @Override
    public void compartmentalize(Mob host) {
        float anxietyLens = this.getPersonalityTrait("anxiety");

        if (this.getValence() < 0.0F) {
            // Fear responses scale heavily off anxiety and drop instantly with low arousal
            this.setSurvival((-this.getValence() * this.getArousal()) * (1.0F + anxietyLens));
        } else {
            this.setSurvival(0.0F);
        }

        // Map biological survival drives directly to hunger weights
        this.setSelfInterest(this.hunger);

        // Peaceful playfulness modifiers
        if (!this.predatorNearby && this.getValence() > 0.0F) {
            this.setAnalytical(this.getPersonalityTrait("playfulness") * 0.3F);
        } else {
            this.setAnalytical(0.0F);
        }
    }

    @Override
    public void regulate(Mob host) {
        // If panic overrides baseline comfort, force maximum immediate flight pressure
        if (this.getSurvival() > 0.7F) {
            this.setSelfInterest(0.0F); // Disregard hunger state entirely while fleeing
        }
    }

    @Override
    protected void populateActionCandidates(Mob host) {
        this.availableActions.clear();

        // Cows can always choose to graze when safe, but custom flight loops can register on survival spikes
        this.registerAction(new CowGrazeAction());
    }
}