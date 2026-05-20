package com.fermine.umweltlite.processor;


import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.processor.specialist.*;
import net.minecraft.world.entity.Mob;

public class ProcessorLoader {
    public static void initializeEngine(Mob mob, UmweltEngine engine) {
        engine.getProcessors().clear();
        engine.addProcessor(new KnowledgeProcessor());
        engine.addProcessor(new WillProcessor());
        engine.addProcessor(new SocialProcessor());
        engine.addProcessor(new SteeringProcessor());
        engine.addProcessor(new SurvivalProcessor());

    }
}