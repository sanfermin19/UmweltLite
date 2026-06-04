package com.fermine.umweltlite.impl.events;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.MemoryAPI;
import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.impl.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.impl.registry.UmweltEntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles systemic host hijacking and cognitive initiation routines.
 */
@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltSpawnEvents {

    private static final String RESET_TAG = "UmweltHijacked";
    private static final String AWAKENED_TAG = "UmweltMindAwakened";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob originalMob)) return;

        // --- HIJACKING LOGIC ---
        if (!(originalMob instanceof IUmweltEntity)) {
            if (originalMob.getPersistentData().contains(RESET_TAG)) return;

            if (event.getLevel() instanceof ServerLevel serverLevel) {
                // Call the registry directly
                Optional<EntityType<? extends Mob>> replacementType = UmweltEntityRegistry.getReplacementFor(originalMob);

                if (replacementType.isPresent()) {
                    replaceAndCancel(originalMob, replacementType.get(), serverLevel, event);
                    return;
                }
            }
        }

        // --- AWAKENING LOGIC ---
        if (originalMob instanceof IUmweltEntity umweltMob) {
            if (originalMob.getPersistentData().getBoolean(AWAKENED_TAG)) return;

            UmweltEngine engine = umweltMob.getUmweltEngine();
            if (engine == null) return;

            originalMob.getPersistentData().putBoolean(AWAKENED_TAG, true);

            AABB area = originalMob.getBoundingBox().inflate(10.0);
            var peers = event.getLevel().getEntities(
                    originalMob,
                    area,
                    e -> e.getType() == originalMob.getType() && e != originalMob
            );

            float empathy = PersonalityAPI.getTrait(engine, "empathy");
            float anxiety = PersonalityAPI.getTrait(engine, "anxiety");

            if (!peers.isEmpty()) {
                CompoundTag context = new CompoundTag();
                context.putString("event", "birth_socialization");

                MemoryAPI.injectMemory(engine, originalMob.position(), new EmotionalMap(0.6f, 0.2f, 0.5f), context);

                float socialBonus = (peers.size() * 0.1f) * empathy;
                float socialStress = (peers.size() * 0.05f) * anxiety;

                float finalValence = Mth.clamp(0.5f + socialBonus, -1.0f, 1.0f);
                float finalArousal = Mth.clamp(0.2f + socialStress, -1.0f, 1.0f);

                EmotionAPI.setEmotionalState(engine, finalValence, finalArousal, 1.0f);
                KnowledgeAPI.setEmotionalBias(engine, originalMob.getType(), finalValence, finalArousal);
            } else {
                float bravery = PersonalityAPI.getTrait(engine, "bravery");
                float startingValence = Mth.clamp(0.5f + (bravery * 0.2f), -1.0f, 1.0f);
                float startingArousal = Mth.clamp(0.3f * anxiety, -1.0f, 1.0f);

                EmotionAPI.setEmotionalState(engine, startingValence, startingArousal, 1.0f);
                KnowledgeAPI.setEmotionalBias(engine, originalMob.getType(), 0.6f, 0.2f);
            }
        }
    }

    private static void replaceAndCancel(Mob original, EntityType<? extends Mob> targetType, ServerLevel level, EntityJoinLevelEvent event) {
        event.setCanceled(true);
        original.discard();

        level.getServer().execute(() -> {
            Mob umweltMob = targetType.create(level);
            if (umweltMob == null) return;

            CompoundTag originalNbt = new CompoundTag();
            original.saveWithoutId(originalNbt);
            umweltMob.load(originalNbt);

            umweltMob.setUUID(UUID.randomUUID());

            umweltMob.moveTo(original.getX(), original.getY(), original.getZ(), original.getYRot(), original.getXRot());
            umweltMob.setYHeadRot(original.getYHeadRot());
            umweltMob.setYBodyRot(original.getYRot());

            umweltMob.getPersistentData().putBoolean(RESET_TAG, true);

            level.addFreshEntityWithPassengers(umweltMob);
        });
    }
}