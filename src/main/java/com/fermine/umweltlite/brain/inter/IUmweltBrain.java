package com.fermine.umweltlite.brain.inter;

import com.fermine.umweltlite.brain.inter.lobes.IFrontalLobe;
import com.fermine.umweltlite.brain.inter.lobes.IOccipitalLobe;
import com.fermine.umweltlite.brain.inter.lobes.IParietalLobe;
import com.fermine.umweltlite.brain.inter.lobes.ITemporalLobe;
import com.fermine.umweltlite.brain.inter.sections.IAmygdala;
import com.fermine.umweltlite.brain.inter.sections.IBrainStem;
import com.fermine.umweltlite.brain.inter.sections.IHippocampus;
import net.minecraft.world.entity.LivingEntity;

public interface IUmweltBrain {
    void tickBrain(LivingEntity host);

    // Deep Subcortical Structures
    IAmygdala getAmygdala();
    IHippocampus getHippocampus();
    IBrainStem getBrainStem();

    // Cortical Lobes
    IOccipitalLobe getOccipitalLobe();
    ITemporalLobe getTemporalLobe();
    IParietalLobe getParietalLobe();
    IFrontalLobe getFrontalLobe();

    net.minecraft.nbt.CompoundTag serializeBrain();
    void deserializeBrain(net.minecraft.nbt.CompoundTag tag);
}