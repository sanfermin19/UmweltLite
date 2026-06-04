package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.world.phys.Vec3;

public interface IFrontalLobe extends IBrainComponent {
    Vec3 getActiveMotorVector();
    void setActiveMotorVector(Vec3 vector);
}