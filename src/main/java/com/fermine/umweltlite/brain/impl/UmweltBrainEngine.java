package com.fermine.umweltlite.brain.impl;

import com.fermine.umweltlite.brain.inter.IUmweltAction;
import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core implementation of the 5-stage Cognitive Triad Architecture.
 * Intertwines persistent personality profiles, emotional circumplex calculations, and action arbitration loops.
 */
public abstract class UmweltBrainEngine implements IUmweltBrainPipeline {

    protected final List<IUmweltAction> availableActions = new ArrayList<>();
    protected IUmweltAction activeAction = null;

    // --- Core Triad Personality Registry ---
    protected final Map<String, Float> personalityTraits = new HashMap<>();
    protected long personalitySeed = 0L;
    protected boolean traitsGenerated = false;

    // --- Global Emotional Registers (Circumplex Model) ---
    protected float valence = 0.0f; // -1.0f (Negative/Unpleasant) to 1.0f (Positive/Pleasant)
    protected float arousal = 0.0f; // -1.0f (Low/Deactivated) to 1.0f (High/Activated)

    // --- Core Triad Domain Weights ---
    protected float survivalRegister = 0.0f;
    protected float selfInterestRegister = 0.0f;
    protected float analyticalRegister = 0.0f;

    @Override
    public final void tickBrainPipeline(Mob host) {
        if (host.level().isClientSide()) return;

        // Lazy-init personality traits if the engine was constructed without an NBT hook yet
        if (!this.traitsGenerated) {
            this.ensurePersonalityInitialized(host);
        }

        this.perceive(host);           // Stage 1: Collect environmental metrics & find neighbors
        this.processEmotions(host);    // Stage 2: Atmosphere / Mood baseline synthesis
        this.compartmentalize(host);   // Stage 3: Project inputs into triad values through personality lenses
        this.regulate(host);           // Stage 4: Factor in raw physical energy/metabolic limits
        this.decide(host);             // Stage 5: Democratic action voting matrix run
        this.execute(host);            // Stage 6: Update active action ticks
    }

    /**
     * Stage 1: Gather raw environment inputs, find nearby prey/predators, analyze paths.
     */
    public abstract void perceive(Mob host);

    /**
     * Stage 2: Distill perceptual flags into valence and arousal vector steps.
     */
    protected abstract void processEmotions(Mob host);

    /**
     * Stage 3: Map current environment data and emotional baselines to the Triad domains.
     * Use {@link #getPersonalityTrait(String)} here to scale responses according to character profile.
     */
    public abstract void compartmentalize(Mob host);

    /**
     * Appends situational action choices to the processing register prior to the voting matrix selection pass.
     */
    protected abstract void populateActionCandidates(Mob host);

    /**
     * Explicitly binds an architectural behavior block to this engine instance.
     */
    public void registerAction(IUmweltAction action) {
        if (action != null && !this.availableActions.contains(action)) {
            this.availableActions.add(action);
        }
    }

    @Override
    public void decide(Mob host) {
        this.populateActionCandidates(host);

        if (this.availableActions.isEmpty()) {
            if (this.activeAction != null) {
                this.activeAction.stop(host);
                this.activeAction = null;
            }
            return;
        }

        IUmweltAction highestCandidate = null;
        float maxUrgency = -1.0f;

        // Collect votes based on psychological domain pressure
        for (IUmweltAction candidate : this.availableActions) {
            float urgency = candidate.calculateUrgency(host, this.analyticalRegister, this.survivalRegister, this.selfInterestRegister);
            if (urgency > maxUrgency) {
                maxUrgency = urgency;
                highestCandidate = candidate;
            }
        }

        // Apply hysteresis filter layer to screen out flickering behavior ticks
        if (highestCandidate != null) {
            if (this.activeAction == null) {
                this.activeAction = highestCandidate;
                this.activeAction.start(host);
            } else if (!this.activeAction.getActionId().equals(highestCandidate.getActionId())) {
                float activeUrgency = this.activeAction.calculateUrgency(host, this.analyticalRegister, this.survivalRegister, this.selfInterestRegister);

                // Hysteresis barrier requirement (+0.15F) to intentionally shift executive context focus
                if (maxUrgency > activeUrgency + 0.15f) {
                    this.activeAction.stop(host);
                    this.activeAction = highestCandidate;
                    this.activeAction.start(host);
                }
            }
        }
    }

    protected void execute(Mob host) {
        if (this.activeAction != null) {
            this.activeAction.tick(host);
        }
    }

    protected void ensurePersonalityInitialized(Mob host) {
        if (this.personalitySeed == 0L) {
            this.personalitySeed = host.getUUID().getLeastSignificantBits() ^ host.level().getGameTime();
        }

        RandomSource random = RandomSource.create(this.personalitySeed);
        this.personalityTraits.put("bravery", random.nextFloat());
        this.personalityTraits.put("empathy", random.nextFloat());
        this.personalityTraits.put("anxiety", random.nextFloat());
        this.personalityTraits.put("playfulness", random.nextFloat());
        this.personalityTraits.put("analytical", random.nextFloat());
        this.personalityTraits.put("focus", random.nextFloat());

        this.traitsGenerated = true;
    }

    // --- Accessor Framework ---

    @Override
    public float getValence() { return this.valence; }
    public void setValence(float value) { this.valence = Mth.clamp(value, -1.0f, 1.0f); }

    @Override
    public float getArousal() { return this.arousal; }
    public void setArousal(float value) { this.arousal = Mth.clamp(value, -1.0f, 1.0f); }

    @Override
    public float getSurvival() { return this.survivalRegister; }
    public void setSurvival(float value) { this.survivalRegister = Mth.clamp(value, 0.0f, 1.0f); }

    @Override
    public float getSelfInterest() { return this.selfInterestRegister; }
    public void setSelfInterest(float value) { this.selfInterestRegister = Mth.clamp(value, 0.0f, 1.0f); }

    @Override
    public float getAnalytical() { return this.analyticalRegister; }
    public void setAnalytical(float value) { this.analyticalRegister = Mth.clamp(value, 0.0f, 1.0f); }

    @Override
    public float getPersonalityTrait(String trait) {
        return this.personalityTraits.getOrDefault(trait, 0.5f);
    }

    @Override
    public String getActiveStageName() {
        return this.activeAction != null ? this.activeAction.getActionId().toString() : "umwelt:idle";
    }

    // --- NeoForge Serialization (Handles Seed preservation for identity matching) ---

    @Override
    public CompoundTag serializePipeline() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("PersonalitySeed", this.personalitySeed);
        tag.putFloat("Valence", this.valence);
        tag.putFloat("Arousal", this.arousal);
        tag.putFloat("SurvivalReg", this.survivalRegister);
        tag.putFloat("SelfInterestReg", this.selfInterestRegister);
        tag.putFloat("AnalyticalReg", this.analyticalRegister);
        return tag;
    }

    @Override
    public void deserializePipeline(CompoundTag tag) {
        this.personalitySeed = tag.getLong("PersonalitySeed");

        // Explicitly clear and rebuild trait tables based on loaded seed coordinates
        this.traitsGenerated = false;
        this.personalityTraits.clear();

        if (tag.contains("Valence")) this.valence = tag.getFloat("Valence");
        if (tag.contains("Arousal")) this.arousal = tag.getFloat("Arousal");
        if (tag.contains("SurvivalReg")) this.survivalRegister = tag.getFloat("SurvivalReg");
        if (tag.contains("SelfInterestReg")) this.selfInterestRegister = tag.getFloat("SelfInterestReg");
        if (tag.contains("AnalyticalReg")) this.analyticalRegister = tag.getFloat("AnalyticalReg");
    }
}