package com.fermine.umweltlite.example;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.example.client.BearBrainRenderLayer;
import com.fermine.umweltlite.example.entity.brain.UmweltCow;
import com.fermine.umweltlite.example.entity.goals.UmweltPig;
import com.fermine.umweltlite.example.entity.goals.UmweltSheep;
import com.fermine.umweltlite.example.entity.brain.UmweltPolarBear;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.PolarBear;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class UmweltEntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, UmweltLite.MODID);

    // --- Entity Registrations ---

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltSheep>> UMWELT_SHEEP =
            ENTITIES.register("umwelt_sheep", () ->
                    EntityType.Builder.of(UmweltSheep::new, MobCategory.CREATURE).sized(0.9F, 1.3F).build("umwelt_sheep"));

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltPig>> UMWELT_PIG =
            ENTITIES.register("umwelt_pig", () ->
                    EntityType.Builder.of(UmweltPig::new, MobCategory.CREATURE).sized(0.9F, 0.9F).build("umwelt_pig"));

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltCow>> UMWELT_COW =
            ENTITIES.register("umwelt_cow", () ->
                    EntityType.Builder.of(UmweltCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).build("umwelt_cow"));

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltPolarBear>> UMWELT_POLAR_BEAR =
            ENTITIES.register("umwelt_polar_bear", () ->
                    EntityType.Builder.of(UmweltPolarBear::new, MobCategory.CREATURE).sized(1.4F, 1.4F).build("umwelt_polar_bear"));

    // --- Hijacking Registry Swap-Map Architecture ---
    private static final Map<Class<? extends Mob>, Supplier<? extends EntityType<? extends Mob>>> SWAP_MAP = new LinkedHashMap<>();

    static {
        registerSwap(Sheep.class, UMWELT_SHEEP);
        registerSwap(Pig.class, UMWELT_PIG);
        registerSwap(Cow.class, UMWELT_COW);
        registerSwap(PolarBear.class, UMWELT_POLAR_BEAR); // Intercepts vanilla ice tundra spawns entirely
    }

    private static void registerSwap(Class<? extends Mob> vanilla, Supplier<? extends EntityType<? extends Mob>> replacement) {
        SWAP_MAP.put(vanilla, replacement);
    }

    public static Optional<EntityType<? extends Mob>> getReplacementFor(Mob mob) {
        for (Map.Entry<Class<? extends Mob>, Supplier<? extends EntityType<? extends Mob>>> entry : SWAP_MAP.entrySet()) {
            if (entry.getKey().isInstance(mob)) {
                return Optional.ofNullable(entry.getValue().get());
            }
        }
        return Optional.empty();
    }

    // --- Attributes Deployment ---
    @EventBusSubscriber(modid = UmweltLite.MODID)
    public static class AttributesRegister {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(UMWELT_SHEEP.get(),
                    Sheep.createAttributes()
                            .add(Attributes.ATTACK_DAMAGE, 2.0D)
                            .build());

            event.put(UMWELT_PIG.get(),
                    Pig.createAttributes()
                            .add(Attributes.ATTACK_DAMAGE, 3.0D)
                            .build());

            event.put(UMWELT_COW.get(),
                    Cow.createAttributes()
                            .add(Attributes.ATTACK_DAMAGE, 2.5D)
                            .build());

            event.put(UMWELT_POLAR_BEAR.get(),
                    PolarBear.createAttributes()
                            .add(Attributes.MAX_HEALTH, 30.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.25D)
                            .add(Attributes.ATTACK_DAMAGE, 6.0D)
                            .build());
        }
    }

    // --- Client-Side Engine Visualization Layer Registration ---
    @EventBusSubscriber(modid = UmweltLite.MODID)
    public static class RenderersRegister {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(UMWELT_SHEEP.get(), SheepRenderer::new);
            event.registerEntityRenderer(UMWELT_PIG.get(), PigRenderer::new);
            event.registerEntityRenderer(UMWELT_COW.get(), CowRenderer::new);
            event.registerEntityRenderer(UMWELT_POLAR_BEAR.get(), PolarBearRenderer::new);
        }

        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            // Find our registered bear proxy and weave the live cognitive color shader processing directly into its stack
            PolarBearRenderer renderer = event.getRenderer(UMWELT_POLAR_BEAR.get());
            if (renderer != null) {
                renderer.addLayer(new BearBrainRenderLayer(renderer));
            }
        }
    }
}