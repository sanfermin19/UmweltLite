package com.fermine.umweltlite.example.brain;

import com.fermine.umweltlite.brain.capability.UmweltBrainRegistry;
import com.fermine.umweltlite.brain.impl.UmweltBrainEngine;
import com.fermine.umweltlite.example.action.BearHuntingAction;
import com.fermine.umweltlite.example.action.BearRestAction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Fox;
import java.util.List;

public class UmweltPolarBearBrain extends UmweltBrainEngine {

    private LivingEntity perceivedPrey = null;
    private float exhaustion = 0.0F;

    @Override
    public ResourceLocation getBrainTypeId() {
        return UmweltBrainRegistry.POLAR_BEAR_BRAIN;
    }

    @Override
    public void perceive(Mob host) {
        // Stage 1: Run sensor checks
        List<Fox> foxes = host.level().getEntitiesOfClass(Fox.class, host.getBoundingBox().inflate(16.0D));
        this.perceivedPrey = !foxes.isEmpty() ? foxes.getFirst() : null;
    }

    @Override
    protected void processEmotions(Mob host) {
        // Stage 2: Distill data into Effect Atmosphere (Valence / Arousal)
        if (this.perceivedPrey != null && this.perceivedPrey.isAlive()) {
            this.setArousal(this.getArousal() + 0.05F); // Alertness rises
            this.setValence(this.getValence() - 0.02F); // Agitation rises
            this.exhaustion = Math.min(1.0F, this.exhaustion + 0.005F);
        } else {
            this.setArousal(this.getArousal() - 0.02F); // Calm down
            this.setValence(this.getValence() + 0.01F); // Contentment returns
            this.exhaustion = Math.max(0.0F, this.exhaustion - 0.01F);
        }
    }

    @Override
    public void compartmentalize(Mob host) {
        // Stage 3: Pass emotions through unique character traits
        float anxietyLens = this.getPersonalityTrait("anxiety");
        float braveryLens = this.getPersonalityTrait("bravery");

        // High arousal + low valence triggers Survival drive, heavily boosted by anxiety
        if (this.getValence() < 0.0F) {
            this.setSurvival((-this.getValence() * this.getArousal()) * (1.0F + anxietyLens));
        } else {
            this.setSurvival(0.0F);
        }

        // Biological fatigue scales the self-interest register up directly
        this.setSelfInterest(this.exhaustion);

        // Social and curiosity mapping defaults to flat baseline for now
        this.setAnalytical(this.getPersonalityTrait("analytical") * 0.2F);
    }

    @Override
    public void regulate(Mob host) {
        // Stage 4: Physiological locks (e.g., exhaustion safety overrides)
        if (this.exhaustion > 0.9F) {
            this.setSelfInterest(1.0F); // Force exhaustion limit
        }
    }

    @Override
    protected void populateActionCandidates(Mob host) {
        this.availableActions.clear();

        // Register behavioral pool candidates dynamically based on sensory inputs
        this.registerAction(new BearRestAction());
        if (this.perceivedPrey != null && this.perceivedPrey.isAlive()) {
            this.registerAction(new BearHuntingAction(this.perceivedPrey));
        }
    }
}