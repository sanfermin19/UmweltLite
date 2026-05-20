package com.fermine.umweltlite;

import com.fermine.umweltlite.api.registry.UmweltRegistryHelper;
import com.fermine.umweltlite.api.command.UmweltCommand;
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fermine.umweltlite.registry.UmweltEntityRegistry;
import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * UmweltLite: A performance-optimized AI framework focusing on
 * Valence, Arousal, and Memory Appraisal.
 */
@Mod(UmweltLite.MODID)
public class UmweltLite {
    public static final String MODID = "umweltlite";

    /**
     * Maps vanilla entity target classes to their respective custom replacement types.
     * Uses Supplier instances to gracefully bypass early registry binding validation checks.
     */
    private static Map<Class<? extends Mob>, Supplier<EntityType<? extends Mob>>> SWAP_MAP;

    public UmweltLite(IEventBus modBus) {
        // Register core mechanics and deferred content registries
        PerceptionRegistry.register(modBus);
        UmweltEntityRegistry.ENTITIES.register(modBus);

        // Register mod lifecycle event listeners
        modBus.addListener(this::registerAttributes);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Safely construct our map routes during the initialization epoch
        initializeSwapRegistry();
    }

    /**
     * Assembles the structural transformation table for the entity hijacking system.
     * Utilizing method references allows Java's target typing to implicitly handle
     * the conversion from concrete EntityType bounds to wildcards, destroying the variance error.
     */
    private void initializeSwapRegistry() {
        ImmutableMap.Builder<Class<? extends Mob>, Supplier<EntityType<? extends Mob>>> builder =
                ImmutableMap.builder();

        // FIX: Using method references (::get) forces the lambda factory to target-type
        // directly to Supplier<EntityType<? extends Mob>>, breaking the variance deadlock!
        builder.put(Sheep.class, UmweltEntityRegistry.UMWELT_SHEEP::get);
        builder.put(Pig.class, UmweltEntityRegistry.UMWELT_PIG::get);

        SWAP_MAP = builder.build();
    }

    /**
     * Looks up whether a standard incoming entity has an upgraded cognitive variant registered.
     * Invoked safely by the thread-decoupled spawn events when entities cross level thresholds.
     *
     * @param mob The base vanilla mob targeting a world layer join.
     * @return An Optional wrapper containing the target upgrade EntityType if a rule matches.
     */
    public static Optional<EntityType<? extends Mob>> getReplacementFor(Mob mob) {
        if (SWAP_MAP == null || mob == null) {
            return Optional.empty();
        }

        for (Map.Entry<Class<? extends Mob>, Supplier<EntityType<? extends Mob>>> entry : SWAP_MAP.entrySet()) {
            if (entry.getKey().isInstance(mob)) {
                return Optional.ofNullable(entry.getValue().get());
            }
        }
        return Optional.empty();
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(UmweltEntityRegistry.UMWELT_SHEEP.get(),
                Sheep.createAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 2.0D) // 1 Heart of damage
                        .build());

        event.put(UmweltEntityRegistry.UMWELT_PIG.get(),
                Pig.createAttributes()
                        .add(Attributes.ATTACK_DAMAGE, 3.0D) // Pigs are beefier! 1.5 Hearts
                        .build());
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        UmweltCommand.register(event.getDispatcher());
    }

    @EventBusSubscriber(modid = UmweltLite.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            UmweltRegistryHelper.registerAllRenderers(event);
        }
    }
}