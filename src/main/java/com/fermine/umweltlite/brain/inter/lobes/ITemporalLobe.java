package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.world.entity.LivingEntity;

public interface ITemporalLobe extends IBrainComponent {
    boolean isRecognizedThreat(LivingEntity entity);
    String getEntitySocialClassification(LivingEntity entity);
}