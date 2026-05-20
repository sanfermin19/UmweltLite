package com.fermine.umweltlite.api.registry;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * UmweltLite Registry Helper
 * Uses Dual-Generics to decouple specific entity types from their vanilla renderers.
 */
public class UmweltRegistryHelper {

    private record RendererEntry(
            Supplier<? extends EntityType<?>> entitySupplier,
            EntityRendererProvider<?> provider
    ) {}

    private static final List<RendererEntry> RENDERER_QUEUES = Collections.synchronizedList(new ArrayList<>());

    /**
     * Register the physical Entity.
     */
    public static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerMob(
            DeferredRegister<EntityType<?>> register,
            String name,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            float width,
            float height) {

        return register.register(name, () ->
                EntityType.Builder.of(factory, category)
                        .sized(width, height)
                        .build(name));
    }

    /**
     * Link the Visual Renderer.
     */
    public static <E extends T, T extends Mob> void bindRenderer(
            Supplier<? extends EntityType<E>> entitySupplier,
            EntityRendererProvider<T> renderer) {

        RENDERER_QUEUES.add(new RendererEntry(entitySupplier, renderer));
    }

    /**
     * Finalize: Called during the Client Mod Bus event.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerAllRenderers(EntityRenderersEvent.RegisterRenderers event) {
        synchronized (RENDERER_QUEUES) {
            for (RendererEntry entry : RENDERER_QUEUES) {
                EntityType<?> type = entry.entitySupplier().get();
                if (type != null) {
                    event.registerEntityRenderer(
                            (EntityType) type,
                            (EntityRendererProvider) entry.provider()
                    );
                }
            }
            RENDERER_QUEUES.clear();
        }
    }
}