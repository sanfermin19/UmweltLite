package com.fermine.umweltlite.engine.sensory.perception;

public record BlockPerception(
        float complexity,   // Structural detail (0-1)
        float intensity,    // Visual brightness/energy (0-1)
        float saturation,   // Color vividness (0-1)
        float roughness     // Physical/Tactile friction (0-1)
) { }
