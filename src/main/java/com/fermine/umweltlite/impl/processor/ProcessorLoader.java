package com.fermine.umweltlite.processor;

import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.processor.specialist.*;
import net.minecraft.world.entity.Mob;

public class ProcessorLoader {
    public static void initializeEngine(Mob mob, UmweltEngine engine) {
        engine.getProcessors().clear();

        // 1. OBSERVERS (Pure data, no movement. Pri 10-20)
        engine.addProcessor(new KnowledgeProcessor());

        // 2. MOTIVATORS (Baseline movement. Pri 30-50)
        engine.addProcessor(new WillProcessor());
        engine.addProcessor(new SocialProcessor());

        // 3. OVERRIDES (Life-saving instincts. Pri 80-100)
        engine.addProcessor(new SurvivalProcessor());
        engine.addProcessor(new SteeringProcessor());
    }
}