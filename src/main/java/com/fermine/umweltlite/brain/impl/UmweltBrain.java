package com.fermine.umweltlite.brain.impl;

import com.fermine.umweltlite.brain.inter.IUmweltBrain;
import com.fermine.umweltlite.brain.inter.sections.IBrainStem;
import com.fermine.umweltlite.brain.inter.sections.IAmygdala;
import com.fermine.umweltlite.brain.inter.sections.IHippocampus;
import com.fermine.umweltlite.brain.inter.lobes.IOccipitalLobe;
import com.fermine.umweltlite.brain.inter.lobes.IParietalLobe;
import com.fermine.umweltlite.brain.inter.lobes.ITemporalLobe;
import com.fermine.umweltlite.brain.inter.lobes.IFrontalLobe;

import com.fermine.umweltlite.brain.impl.sections.BrainStem;
import com.fermine.umweltlite.brain.impl.sections.Amygdala;
import com.fermine.umweltlite.brain.impl.sections.Hippocampus;
import com.fermine.umweltlite.brain.impl.lobes.OccipitalLobe;
import com.fermine.umweltlite.brain.impl.lobes.ParietalLobe;
import com.fermine.umweltlite.brain.impl.lobes.FrontalLobe;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class UmweltBrain implements IUmweltBrain {

    // Concrete instantiations held internally
    private final BrainStem brainStem;
    private final Amygdala amygdala;
    private final Hippocampus hippocampus;
    private final OccipitalLobe occipitalLobe;
    private final ParietalLobe parietalLobe;
    private final FrontalLobe frontalLobe;

    public UmweltBrain() {
        this.brainStem = new BrainStem();
        this.amygdala = new Amygdala(this.brainStem);
        this.hippocampus = new Hippocampus();

        this.occipitalLobe = new OccipitalLobe();
        this.parietalLobe = new ParietalLobe(this.amygdala);

        this.frontalLobe = new FrontalLobe(
                this.brainStem, this.amygdala, this.hippocampus,
                this.occipitalLobe, this.parietalLobe
        );
    }

    @Override
    public void tickBrain(LivingEntity host) {
        // 1. Somatosensory & Autonomic Baseline Updates
        this.brainStem.tick(host);
        this.parietalLobe.tick(host);

        // 2. High-Level Sensory Processing (Vision)
        this.occipitalLobe.tick(host);

        // 3. Emotional Appraisal & Memory Consolidation
        this.amygdala.tick(host);
        this.hippocampus.tick(host);

        // 4. Executive Decision-Making & Motor Strategy Calculation
        this.frontalLobe.tick(host);

        // 5. Motor Injection Physics
        Vec3 brainVelocityDrive = this.frontalLobe.getActiveMotorVector();
        if (brainVelocityDrive != Vec3.ZERO) {
            host.setDeltaMovement(host.getDeltaMovement().add(brainVelocityDrive.scale(0.15)));
        }
    }

    // --- Subcortical Interface Getters ---
    @Override
    public IBrainStem getBrainStem() { return (IBrainStem) this.brainStem; }

    @Override
    public IAmygdala getAmygdala() { return (IAmygdala) this.amygdala; }

    @Override
    public IHippocampus getHippocampus() { return (IHippocampus) this.hippocampus; }

    // --- Cortical Lobe Interface Getters ---
    @Override
    public IOccipitalLobe getOccipitalLobe() { return (IOccipitalLobe) this.occipitalLobe; }

    @Override
    public IParietalLobe getParietalLobe() { return (IParietalLobe) this.parietalLobe; }

    @Override
    public IFrontalLobe getFrontalLobe() { return (IFrontalLobe) this.frontalLobe; }

    /**
     * Satisfies the missed contract method error.
     * Temporary placeholder return value until an auditory sense engine is bound.
     */
    @Override
    public ITemporalLobe getTemporalLobe() {
        return null;
    }

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