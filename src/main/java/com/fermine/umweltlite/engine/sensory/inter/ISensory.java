package com.fermine.umweltlite.engine.sensory.inter;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.sensory.engine.SensoryIntake;
import net.minecraft.world.entity.Mob;

public interface ISensory {
    void pulse(Mob mob, UmweltEngine engine, SensoryIntake intake, int tickCount);

    int scanRate();

    // The Getter you already had
    boolean isEnabled();

    // ADD THIS: The Setter
    void setEnabled(boolean enabled);

    default boolean shouldScan(int tickCount) {
        return isEnabled() && tickCount % scanRate() == 0;
    }
}