package com.fermine.umweltlite.impl.registry;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.impl.entity.UmweltCow;
import com.fermine.umweltlite.impl.entity.UmweltPig;
import com.fermine.umweltlite.impl.entity.UmweltSheep;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.api.distmarker.Dist;
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

    // --- Entity Registrations (Declared first to avoid initialization errors) ---

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltSheep>> UMWELT_SHEEP =
            ENTITIES.register("umwelt_sheep", () ->
                    EntityType.Builder.of(UmweltSheep::new, MobCategory.CREATURE).sized(0.9F, 1.3F).build("umwelt_sheep"));

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltPig>> UMWELT_PIG =
            ENTITIES.register("umwelt_pig", () ->
                    EntityType.Builder.of(UmweltPig::new, MobCategory.CREATURE).sized(0.9F, 0.9F).build("umwelt_pig"));

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltCow>> UMWELT_COW =
            ENTITIES.register("umwelt_cow", () ->
                    EntityType.Builder.of(UmweltCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).build("umwelt_cow"));

    // --- Hijacking Registry ---
    private static final Map<Class<? extends Mob>, Supplier<? extends EntityType<? extends Mob>>> SWAP_MAP = new LinkedHashMap<>();

    static {
        registerSwap(Sheep.class, UMWELT_SHEEP);
        registerSwap(Pig.class, UMWELT_PIG);
        registerSwap(Cow.class, UMWELT_COW);
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

    // --- Event Subscribers ---
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
                            .add(Attributes.ATTACK_DAMAGE, 2.5D) // Bovine headbutt damage
                            .build());
        }
    }

    /**
     * Handles Renderer registration.
     */
    @EventBusSubscriber(modid = UmweltLite.MODID, value = Dist.CLIENT)
    public static class RenderersRegister {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(UMWELT_SHEEP.get(), SheepRenderer::new);
            event.registerEntityRenderer(UMWELT_PIG.get(), PigRenderer::new);
            event.registerEntityRenderer(UMWELT_COW.get(), CowRenderer::new);
        }
    }
}