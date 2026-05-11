package com.fermine.umweltlite.engine.personality.engine;

import java.util.Map;

public record PersonalityTemplate(
        String id,
        Map<String, Float> baseTraits,
        float variance // How much the individual 'DNA' seed can deviate from these bases
) {}
