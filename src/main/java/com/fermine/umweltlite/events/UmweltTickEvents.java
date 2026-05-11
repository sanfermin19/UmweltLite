package com.fermine.umweltlite.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.particles.ParticleOptions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltTickEvents {
    @SubscribeEvent
    public static void onUmweltPulse(EntityTickEvent.Post event) {
        if (event.getEntity().level().getGameTime() % 20 != 0) return;

        if (event.getEntity() instanceof IUmweltEntity umweltMob && event.getEntity() instanceof Mob mob) {
            UmweltEngine engine = umweltMob.getUmweltEngine();
            float v = engine.getEmotionalEngine().getValence();
            float a = engine.getEmotionalEngine().getArousal();


            // PRIORITY 1: THREAT/AGGRESSION (Angry Villager)
            // High Stress + Negative Feeling = Get back!
            if (a > 0.7f && v < 0.1f) {
                spawnStatusParticle(mob, ParticleTypes.ANGRY_VILLAGER);
            }

            // PRIORITY 2: HYSTERIA/SADISM (Witch/Magic)
            // High Stress + Positive Feeling = The "Sadist" sheep
            else if (a > 0.7f && v >= 0.1f) {
                spawnStatusParticle(mob, ParticleTypes.WITCH);
            }

            // PRIORITY 3: GENUINE JOY (Hearts)
            // Calm + Very Positive = The "Yay!" effect
            else if (v > 0.7f && a < 0.5f) {
                spawnStatusParticle(mob, ParticleTypes.HEART);
            }

            // PRIORITY 4: DEEP DEPRESSION (Ink)
            // Calm + Very Negative = The "Void" pig
            else if (v < -0.6f && a < 0.4f) {
                spawnStatusParticle(mob, ParticleTypes.SQUID_INK);
            }
        }
    }

    private static void spawnStatusParticle(Mob mob, ParticleOptions particle) {
        mob.level().addParticle(particle,
                mob.getRandomX(0.5), mob.getRandomY() + 0.5, mob.getRandomZ(0.5), 0, 0, 0);
    }
}
