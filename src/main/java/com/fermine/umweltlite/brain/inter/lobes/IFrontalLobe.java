package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import com.fermine.umweltlite.brain.inter.sections.IPrefrontalCortex;
import net.minecraft.world.phys.Vec3;

public interface IFrontalLobe extends IBrainComponent {
    IPrefrontalCortex getPrefrontalCortex(); // Nested executive function
    
    /**
     * Generates the precise physical movement vector based on cognitive intent.
     */
    Vec3 CalculateMotorOutput();
    
    boolean isMotorFunctionImpaired(); // True if paralyzed, stunned, or experiencing learned helplessness
}