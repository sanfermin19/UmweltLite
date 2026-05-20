package com.fermine.umweltlite.registry;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.api.registry.UmweltRegistryHelper;
import com.fermine.umweltlite.entity.UmweltPig;
import com.fermine.umweltlite.entity.UmweltSheep;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class UmweltEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, UmweltLite.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltSheep>> UMWELT_SHEEP =
            UmweltRegistryHelper.registerMob(
                    ENTITIES,
                    "umwelt_sheep",
                    UmweltSheep::new,
                    MobCategory.CREATURE,
                    0.9F, 1.3F
            );

    public static final DeferredHolder<EntityType<?>, EntityType<UmweltPig>> UMWELT_PIG =
            UmweltRegistryHelper.registerMob(
                    ENTITIES,
                    "umwelt_pig",
                    UmweltPig::new,
                    MobCategory.CREATURE,
                    0.9F, 0.9F
            );

    static {
        UmweltRegistryHelper.bindRenderer(UMWELT_SHEEP, SheepRenderer::new);
        UmweltRegistryHelper.bindRenderer(UMWELT_PIG, PigRenderer::new);
    }
}