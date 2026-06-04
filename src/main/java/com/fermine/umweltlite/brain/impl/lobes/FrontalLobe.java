package com.fermine.umweltlite.brain.impl.lobes;

import com.fermine.umweltlite.brain.inter.lobes.IFrontalLobe;
import com.fermine.umweltlite.brain.inter.sections.IBrainStem;
import com.fermine.umweltlite.brain.inter.sections.IAmygdala;
import com.fermine.umweltlite.brain.inter.sections.IHippocampus;
import com.fermine.umweltlite.brain.inter.lobes.IOccipitalLobe;
import com.fermine.umweltlite.brain.inter.lobes.IParietalLobe;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class FrontalLobe implements IFrontalLobe {

    private final IBrainStem brainStem;
    private final IAmygdala amygdala;
    private final IHippocampus hippocampus;
    private final IOccipitalLobe occipitalLobe;
    private final IParietalLobe parietalLobe;

    private Vec3 activeMotorVector = Vec3.ZERO;

    public FrontalLobe(IBrainStem brainStem, IAmygdala amygdala, IHippocampus hippocampus,
                       IOccipitalLobe occipitalLobe, IParietalLobe parietalLobe) {
        this.brainStem = brainStem;
        this.amygdala = amygdala;
        this.hippocampus = hippocampus;
        this.occipitalLobe = occipitalLobe;
        this.parietalLobe = parietalLobe;
    }

    @Override
    public void tick(LivingEntity host) {
        // Decay structural physical motor drift drivers over time
        if (this.activeMotorVector != null && this.activeMotorVector != Vec3.ZERO) {
            this.activeMotorVector = this.activeMotorVector.scale(0.8);
            if (this.activeMotorVector.lengthSqr() < 0.001) {
                this.activeMotorVector = Vec3.ZERO;
            }
        }
    }

    @Override
    public Vec3 getActiveMotorVector() {
        return this.activeMotorVector;
    }

    @Override
    public void setActiveMotorVector(Vec3 vector) {
        this.activeMotorVector = vector;
    }

    @Override
    public void reset() {
        this.activeMotorVector = Vec3.ZERO;
    }
}