package com.fermine.umweltlite.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.MemoryAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltDeathEvents {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof IUmweltEntity && event.getEntity() instanceof Mob victimMob) {
            var source = event.getSource();
            var killer = source.getEntity();

            // 1. BROADCAST TO WITNESSES
            AABB panicZone = victimMob.getBoundingBox().inflate(15.0);
            var witnesses = victimMob.level().getEntitiesOfClass(Mob.class, panicZone,
                    e -> e instanceof IUmweltEntity && e != victimMob);

            for (Mob witness : witnesses) {
                UmweltEngine witnessEngine = ((IUmweltEntity) witness).getUmweltEngine();
                float empathy = PersonalityAPI.getTrait(witnessEngine, "empathy");

                // Emotional impact on the witness scaled by their empathy
                // Highly empathetic pigs "grieve" harder (lower valence)
                float grief = -0.4f * empathy;
                float shock = 0.5f;

                EmotionAPI.setValence(witnessEngine, Math.max(-1.0f, witnessEngine.getEmotionalEngine().getValence() + grief));
                EmotionAPI.setArousal(witnessEngine, Math.min(1.0f, witnessEngine.getEmotionalEngine().getArousal() + shock));

                // Record the memory of seeing a friend die here
                CompoundTag context = new CompoundTag();
                context.putString("type", "witnessed_death");
                context.putString("victim", victimMob.getName().getString());

                MemoryAPI.injectMemory(
                        witnessEngine,
                        victimMob.position(),
                        new EmotionalMap(-0.9f, 0.7f, 0.5f),
                        context
                );

                // If there's a killer, the whole herd now fears them
                if (killer != null) {
                    KnowledgeAPI.setEmotionalBias(witnessEngine, killer.getType(), -1.0f, 0.9f);
                }

                // Visual for the witness "reacting"
                if (witness.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                            witness.getX(), witness.getY() + 1.2, witness.getZ(), 3, 0.1, 0.1, 0.1, 0);
                }
            }
        }
    }
}
