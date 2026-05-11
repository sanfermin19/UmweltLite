package com.fermine.umweltlite.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.engine.MemoryAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltSpawnEvents {

    @SubscribeEvent
    public static void onFirstBreath(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof IUmweltEntity umweltMob && event.getEntity() instanceof Mob mob) {
            UmweltEngine engine = umweltMob.getUmweltEngine();

            // 1. LIFECYCLE CHECK: Is this a chunk-load reload, or actual birth?
            if (!engine.getMemoryEngine().getShortTermMemories().isEmpty() || !engine.getKnowledgeEngine().getFactMap().isEmpty()) {
                System.out.println("Welcome back: " + mob.getName().getString() + " (Restored from NBT)");
                return;
            }

            // 2. TRUE BIRTH LOGIC (Only runs the very first time the entity is generated in the world)
            AABB area = mob.getBoundingBox().inflate(10.0);
            var peers = event.getLevel().getEntities(mob, area, e -> e.getType() == mob.getType() && e != mob);

            float empathy = PersonalityAPI.getTrait(engine, "empathy");
            float anxiety = PersonalityAPI.getTrait(engine, "anxiety");

            if (!peers.isEmpty()) {
                CompoundTag context = new CompoundTag();
                context.putString("event", "birth_socialization");

                MemoryAPI.injectMemory(
                        engine,
                        mob.position(),
                        new EmotionalMap(0.6f, 0.2f, 0.5f),
                        context
                );

                float socialBonus = (peers.size() * 0.1f) * empathy;
                float socialStress = (peers.size() * 0.05f) * anxiety;

                float finalValence = Mth.clamp(0.5f + socialBonus, 0f, 1f);
                float finalArousal = Mth.clamp(0.2f + socialStress, 0f, 1f);

                EmotionAPI.setEmotionalState(engine, finalValence, finalArousal, 1.0f);
                KnowledgeAPI.setEmotionalBias(engine, mob.getType(), finalValence, finalArousal);
            } else {
            // 3. SOLO BIRTH BUFFER: Preventing the "Instant Void"
            float bravery = PersonalityAPI.getTrait(engine, "bravery");

            float startingValence = 0.5f + (bravery * 0.2f);
            float startingArousal = 0.3f * anxiety;

            EmotionAPI.setEmotionalState(engine, startingValence, startingArousal, 1.0f);

            KnowledgeAPI.setEmotionalBias(engine, mob.getType(), 0.6f, 0.2f);
            }
        }
    }
}