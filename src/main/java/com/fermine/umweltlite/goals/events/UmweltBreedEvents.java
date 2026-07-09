package com.fermine.umweltlite.goals.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.emotion.map.AttachmentMap;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltBreedEvents {

    // Core traits that define the mob's baseline behavior
    private static final String[] HEREDITARY_TRAITS = {
            "bravery", "anxiety", "empathy", "curiosity", "analytical", "focus"
    };

    @SubscribeEvent
    public static void onBabySpawn(net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent event) {
        Entity child = event.getChild();
        Entity parentA = event.getParentA();
        Entity parentB = event.getParentB();

        if (child instanceof IUmweltEntity umweltChild &&
                parentA instanceof IUmweltEntity umweltParentA &&
                parentB instanceof IUmweltEntity umweltParentB) {

            UmweltEngine childEngine = umweltChild.getUmweltEngine();
            UmweltEngine engineA = umweltParentA.getUmweltEngine();
            UmweltEngine engineB = umweltParentB.getUmweltEngine();

            if (childEngine == null || engineA == null || engineB == null) return;

            // 1. Genetic Trait Inheritance
            inheritTraits(childEngine, engineA, engineB);

            // 2. Instant Familial Bonding (Child to Parents)
            // High valence (trust), low arousal (calm), high bond (attachment)
            AttachmentMap parentalBond = new AttachmentMap(0.8f, -0.2f, 0.9f);
            EmotionAPI.setSocialAttachment(childEngine, parentA.getUUID(), parentalBond);
            EmotionAPI.setSocialAttachment(childEngine, parentB.getUUID(), parentalBond);

            // 3. Parents Bond to Child
            AttachmentMap childBond = new AttachmentMap(0.9f, 0.1f, 1.0f);
            EmotionAPI.setSocialAttachment(engineA, child.getUUID(), childBond);
            EmotionAPI.setSocialAttachment(engineB, child.getUUID(), childBond);

            // (Optional) If you have the KnowledgeAPI hooked up, this is where
            // you'd pull the bias maps from A and B, average them, and inject
            // into the child so it inherits fear of players if the parents were hunted.
        }
    }

    /**
     * Averages the traits of both parents and applies a slight genetic mutation.
     * Uses PersonalityAPI.overrideTrait to ensure the genetics bypass the default template.
     */
    private static void inheritTraits(UmweltEngine child, UmweltEngine pA, UmweltEngine pB) {
        for (String trait : HEREDITARY_TRAITS) {
            float valA = PersonalityAPI.getTrait(pA, trait);
            float valB = PersonalityAPI.getTrait(pB, trait);

            // Average out the parents
            float baseInheritance = (valA + valB) / 2.0f;

            // Introduce a -0.05 to +0.05 mutation for genetic drift
            float mutation = (float) (Math.random() * 0.1) - 0.05f;
            float finalTrait = Math.max(0.0f, Math.min(1.0f, baseInheritance + mutation));

            PersonalityAPI.overrideTrait(child, trait, finalTrait);
        }
    }
}