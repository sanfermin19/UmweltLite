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
 * Handles systemic host hijacking and cognitive initiation routines
 * for the UmweltLite AI framework.
 */
@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltSpawnEvents {

    private static final String RESET_TAG = "UmweltHijacked";
    private static final String AWAKENED_TAG = "UmweltMindAwakened";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (!(event.getEntity() instanceof Mob originalMob)) return;

        // Swaps the instance mob
        if (!(originalMob instanceof IUmweltEntity)) {
            // Prevent recursive cascading loops when processing historical entries
            if (originalMob.getPersistentData().contains(RESET_TAG)) return;

            if (event.getLevel() instanceof ServerLevel serverLevel) {
                // Query our centralized main mod registry map dynamically
                Optional<EntityType<? extends Mob>> replacementType = UmweltLite.getReplacementFor(originalMob);

                if (replacementType.isPresent()) {
                    replaceAndCancel(originalMob, replacementType.get(), serverLevel, event);
                    return;
                }
            }
        }

        // Mob "wakes" up
        if (originalMob instanceof IUmweltEntity umweltMob) {
            // Hard stop if this entity has already experienced neural initialization in a past chunk tick
            if (originalMob.getPersistentData().getBoolean(AWAKENED_TAG)) return;

            UmweltEngine engine = umweltMob.getUmweltEngine();
            if (engine == null) return;

            // Mark mind state as explicitly initialized to secure chunk reloads permanently
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

                MemoryAPI.injectMemory(
                        engine,
                        originalMob.position(),
                        new EmotionalMap(0.6f, 0.2f, 0.5f),
                        context
                );

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

    /**
     * Safely schedules the displacement of a standard entity structure with an upgraded cognitive host variant.
     */
    private static void replaceAndCancel(Mob original, EntityType<? extends Mob> targetType, ServerLevel level, EntityJoinLevelEvent event) {
        // Step A: Cancel the incoming entity join event to prevent standard world registration pipeline tracking
        event.setCanceled(true);
        original.discard();

        // Step B: Submit task to server thread scheduler execution window to avoid mid-loop mutations
        level.getServer().execute(() -> {
            Mob umweltMob = targetType.create(level);
            if (umweltMob == null) return;

            // Deep serialize data properties to ensure equipment mappings perfectly match
            CompoundTag originalNbt = new CompoundTag();
            original.saveWithoutId(originalNbt);
            umweltMob.load(originalNbt);

            // FIX: Regenerate UUID explicitly to prevent tracking clashes with the dying host entity
            umweltMob.setUUID(UUID.randomUUID());

            // Deep clone positional layout configurations
            umweltMob.moveTo(original.getX(), original.getY(), original.getZ(), original.getYRot(), original.getXRot());
            umweltMob.setYHeadRot(original.getYHeadRot());
            umweltMob.setYBodyRot(original.getYRot());

            // Stamp tracking security variables to skip phase recursion entirely
            umweltMob.getPersistentData().putBoolean(RESET_TAG, true);

            // Inject modified asset stack directly into level systems at a safe thread epoch boundary
            level.addFreshEntityWithPassengers(umweltMob);
        });
    }
}