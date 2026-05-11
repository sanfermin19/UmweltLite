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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltInteractEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof IUmweltEntity umweltMob && event.getEntity() instanceof Mob mob) {
            UmweltEngine engine = umweltMob.getUmweltEngine();
            var source = event.getSource();
            var attacker = source.getEntity();

            // 1. PERSONALITY DATA
            float anxiety = PersonalityAPI.getTrait(engine, "anxiety");
            float bravery = PersonalityAPI.getTrait(engine, "bravery");

            // 2. EMOTIONAL SHIFT (The Adrenaline Spike)
            // Brave mobs spike less Arousal (stay cooler), Anxious mobs spike harder.
            float arousalSpike = (0.3f + (anxiety * 0.4f)) - (bravery * 0.1f);
            float currentArousal = engine.getEmotionalEngine().getArousal();
            EmotionAPI.setArousal(engine, Math.min(1.0f, currentArousal + arousalSpike));
            EmotionAPI.setValence(engine, -0.7f); // Pain is always negative

            // 3. TARGETING (Crucial for the Attack Goal)
            if (attacker instanceof LivingEntity livingAttacker) {
                mob.setLastHurtByMob(livingAttacker);

                // If the mob is brave enough, force it to target the player immediately
                if (bravery > 0.6f) {
                    mob.setTarget(livingAttacker);
                }
            }

            // 4. MEMORY & BIAS
            if (attacker != null) {
                KnowledgeAPI.setEmotionalBias(engine, attacker.getType(), -0.9f, 0.8f);
            }

            CompoundTag context = new CompoundTag();
            context.putString("type", "trauma");
            MemoryAPI.injectMemory(engine, mob.position(), new EmotionalMap(-0.9f, 0.9f, 0.5f), context);

            // 5. VISUAL FEEDBACK
            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, mob.getX(), mob.getY() + 1.2, mob.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                if (engine.getEmotionalEngine().getValence() < 0.1f) {
                    serverLevel.sendParticles(ParticleTypes.SQUID_INK, mob.getX(), mob.getY() + 0.8, mob.getZ(), 8, 0.3, 0.3, 0.3, 0.02);
                }
            }
        }
    }
}