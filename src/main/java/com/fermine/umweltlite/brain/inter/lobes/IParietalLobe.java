package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;

public interface IParietalLobe extends IBrainComponent {
    void reflectPhysicalDamage(DamageSource source, float amount);
    float getPainIndex();
    float getThermalComfort();
    BlockPos getLastKnownPosition();
}