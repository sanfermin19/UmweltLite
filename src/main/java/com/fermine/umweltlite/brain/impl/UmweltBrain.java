package com.fermine.umweltlite.brain.impl;

import com.fermine.umweltlite.brain.action.*;
import com.fermine.umweltlite.brain.inter.IUmweltBrain;
import com.fermine.umweltlite.brain.inter.sections.IBrainStem;
import com.fermine.umweltlite.brain.inter.sections.IAmygdala;
import com.fermine.umweltlite.brain.inter.sections.IHippocampus;
import com.fermine.umweltlite.brain.inter.sections.IPrefrontalCortex;
import com.fermine.umweltlite.brain.inter.lobes.IOccipitalLobe;
import com.fermine.umweltlite.brain.inter.lobes.IParietalLobe;
import com.fermine.umweltlite.brain.inter.lobes.ITemporalLobe;
import com.fermine.umweltlite.brain.inter.lobes.IFrontalLobe;

import com.fermine.umweltlite.brain.impl.sections.BrainStem;
import com.fermine.umweltlite.brain.impl.sections.Amygdala;
import com.fermine.umweltlite.brain.impl.sections.Hippocampus;
import com.fermine.umweltlite.brain.impl.sections.PrefrontalCortex;
import com.fermine.umweltlite.brain.impl.lobes.OccipitalLobe;
import com.fermine.umweltlite.brain.impl.lobes.ParietalLobe;
import com.fermine.umweltlite.brain.impl.lobes.TemporalLobe;
import com.fermine.umweltlite.brain.impl.lobes.FrontalLobe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UmweltBrain implements IUmweltBrain {

    // A thread-safe queue to capture emergent actions from various brain lobes/processors
    private final Queue<IBrainAction> actionQueue = new ConcurrentLinkedQueue<>();

    // Tracks the most recently selected action from the utility pool for diagnostics
    private String lastSelectedActionId = "idle";

    // Concrete instantiations held internally
    private final BrainStem brainStem;
    private final Amygdala amygdala;
    private final Hippocampus hippocampus;
    private final PrefrontalCortex prefrontalCortex;

    private final OccipitalLobe occipitalLobe;
    private final ParietalLobe parietalLobe;
    private final TemporalLobe temporalLobe;
    private final FrontalLobe frontalLobe;

    public UmweltBrain() {
        this.brainStem = new BrainStem();
        this.amygdala = new Amygdala(this.brainStem);
        this.hippocampus = new Hippocampus();
        this.prefrontalCortex = new PrefrontalCortex(this.amygdala, this.brainStem);

        this.occipitalLobe = new OccipitalLobe();
        this.parietalLobe = new ParietalLobe(this.amygdala);
        this.temporalLobe = new TemporalLobe();

        this.frontalLobe = new FrontalLobe(
                this.brainStem, this.amygdala, this.hippocampus,
                this.occipitalLobe, this.parietalLobe
        );
    }

    @Override
    public void tickBrain(LivingEntity host) {
        if (host.level().isClientSide()) return;

        // 1. Somatosensory & Autonomic Baseline Updates
        this.brainStem.tick(host);
        this.parietalLobe.tick(host);

        // 2. High-Level Sensory Processing (Vision & Identification)
        this.occipitalLobe.tick(host);
        this.temporalLobe.tick(host);

        // 3. Emotional Appraisal, Memory Consolidation & Will Calculation
        this.amygdala.tick(host);
        this.hippocampus.tick(host);
        this.prefrontalCortex.tick(host);

        // 4. Executive Decision-Making & Motor Strategy Calculation
        this.frontalLobe.tick(host);

        // 5. Decider Matrix - Let the executive center autonomously select actions
        // Evaluates once per second (20 ticks) to keep performance clean and human-like
        if (host.tickCount % 20 == 0) {
            evaluateCognitiveDecider(host);
        }

        // 6. Motor Injection Physics (Kept for subtle physical drift forces)
        Vec3 brainVelocityDrive = this.frontalLobe.getActiveMotorVector();
        if (brainVelocityDrive != null && brainVelocityDrive != Vec3.ZERO) {
            host.setDeltaMovement(host.getDeltaMovement().add(brainVelocityDrive.scale(0.15)));
        }
    }

    /**
     * Central routing engine that parses current neuro-states and queues physical goals.
     */
    private void evaluateCognitiveDecider(LivingEntity host) {
        java.util.List<WeightedBrainAction> actionPool = new java.util.ArrayList<>();

        // --- STEP 1: Gather independent drives from subcortical structures ---

        // Fear Drive (From Amygdala)
        float panic = this.amygdala.getPanicFloater();
        actionPool.add(new WeightedBrainAction(new MoveBackwardAction(0.35F), panic * 1.5F));

        // Hunger/Energy Drive (From BrainStem)
        float exhaustion = this.brainStem.getExhaustion();
        actionPool.add(new WeightedBrainAction(new DestroyBlockAction(host.blockPosition().below()), exhaustion));

        // --- OPTION TWO: IMPLEMENT BIOLOGICAL SYNAPTIC NOISE ---
        // Generates an unpredictable sensory drift between -0.06 and +0.06 every second
        float wanderNoise = (host.getRandom().nextFloat() - 0.5f) * 0.12f;

        // Curiosities / Idle Wander Drive (Baseline Instinct)
        // Scaled back slightly if the Prefrontal Cortex is feeling decision fatigue
        float focusMultiplier = 1.0f - this.prefrontalCortex.getDecisionFatigue();

        // Applying the noise shifts balances dynamically so forward and looking can trade places
        actionPool.add(new WeightedBrainAction(new MoveForwardAction(0.20F), (0.25F + wanderNoise) * focusMultiplier));
        actionPool.add(new WeightedBrainAction(new LookAroundAction(30.0F), (0.28F - wanderNoise) * focusMultiplier));

        // Combat Drive (If an enemy exists)
        if (host.getKillCredit() != null) {
            float aggression = this.amygdala.getAggressionFloater();
            actionPool.add(new WeightedBrainAction(new AttackEntityAction(host.getKillCredit()), aggression * 2.0F));
        }

        // --- STEP 2: The Executive Snapshot Selection ---
        WeightedBrainAction highestPriority = actionPool.stream()
                .max(java.util.Comparator.comparingDouble(WeightedBrainAction::getWeight))
                .orElse(null);

        // --- STEP 3: Submit the winning cognitive strategy ---
        // Restored complete null check to prevent server crashes if pool evaluation fails
        if (highestPriority.getWeight() > 0.10F) {
            this.lastSelectedActionId = highestPriority.getAction().getActionId();
            this.submitAction(highestPriority.getAction());
        } else {
            this.lastSelectedActionId = "idle";
        }
    }

    /**
     * Enqueues a brain action to be synchronized and executed on the next tick.
     */
    public void submitAction(IBrainAction action) {
        this.actionQueue.add(action);
    }

    /**
     * Called during the entity's capability tick processing loop.
     */
    public void tickActions(LivingEntity entity) {
        if (entity.level().isClientSide()) return;

        // Automatically run our full biological tick stack right before processing actions
        this.tickBrain(entity);

        while (!actionQueue.isEmpty()) {
            IBrainAction action = actionQueue.poll();
            if (action != null) {
                // 1. Run local server-side logical adjustments (Navigation, Look controls)
                action.execute(entity);

                // 2. Broadcast packet to tracking clients
                sendActionPacketToTrackingClients(entity, action);
            }
        }
    }

    private void sendActionPacketToTrackingClients(LivingEntity entity, IBrainAction action) {
        // Network hook for syncing clients if animations are attached later
    }

    public String getLastSelectedActionId() {
        return this.lastSelectedActionId;
    }

    // --- Subcortical Interface Getters ---
    @Override
    public IBrainStem getBrainStem() { return this.brainStem; }

    @Override
    public IAmygdala getAmygdala() { return this.amygdala; }

    @Override
    public IHippocampus getHippocampus() { return this.hippocampus; }

    public IPrefrontalCortex getPrefrontalCortex() { return this.prefrontalCortex; }

    // --- Cortical Lobe Interface Getters ---
    @Override
    public IOccipitalLobe getOccipitalLobe() { return this.occipitalLobe; }

    @Override
    public IParietalLobe getParietalLobe() { return this.parietalLobe; }

    @Override
    public IFrontalLobe getFrontalLobe() { return this.frontalLobe; }

    @Override
    public ITemporalLobe getTemporalLobe() { return this.temporalLobe; }

    // --- Data Persistence Layer (NBT) ---
    @Override
    public CompoundTag serializeBrain() {
        CompoundTag tag = new CompoundTag();
        tag.put("HippocampusData", this.hippocampus.saveMemoryData());
        tag.putFloat("ChronicPanic", this.amygdala.getPanicFloater());
        tag.putFloat("ChronicAggression", this.amygdala.getAggressionFloater());
        tag.putFloat("MetabolicExhaustion", this.brainStem.getExhaustion());
        return tag;
    }

    @Override
    public void deserializeBrain(CompoundTag tag) {
        if (tag.contains("HippocampusData")) {
            this.hippocampus.loadMemoryData(tag.getCompound("HippocampusData"));
        }
        if (tag.contains("ChronicPanic")) {
            this.amygdala.triggerShock(tag.getFloat("ChronicPanic"));
        }
        if (tag.contains("ChronicAggression")) {
            this.amygdala.provokeAggression(tag.getFloat("ChronicAggression"));
        }
        if (tag.contains("MetabolicExhaustion")) {
            this.brainStem.exertEnergy(tag.getFloat("MetabolicExhaustion"));
        }
    }
}