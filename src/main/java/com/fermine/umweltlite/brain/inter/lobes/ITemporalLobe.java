package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.world.phys.Vec3;

public interface ITemporalLobe extends IBrainComponent {
    /**
     * Registers a sound event heard in the environment.
     * @param soundId The registry name of the sound.
     * @param source The physical location the sound originated from.
     * @param volume How loud the sound was to the organism.
     */
    void processAuditoryInput(String soundId, Vec3 source, float volume);

    float getAcousticSensitivity(); // Scale of 0.0 to 1.0 (e.g., higher when panicked)
}