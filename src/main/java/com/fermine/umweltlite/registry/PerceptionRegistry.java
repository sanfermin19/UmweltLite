package com.fermine.umweltlite.registry;


import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PerceptionRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, UmweltLite.MODID);

    /**
     * The core Brain Attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<UmweltEngine>> PERCEPTION =
            ATTACHMENT_TYPES.register("perception", () -> AttachmentType.builder((holder) -> {
                        if (holder instanceof Mob mob) {
                            return new UmweltEngine(mob);
                        }
                        return new UmweltEngine(null);
                    })
                    .serialize(new IAttachmentSerializer<>() {
                        @Override
                        public @NotNull UmweltEngine read(@NotNull IAttachmentHolder holder, @NotNull Tag tag, @NotNull HolderLookup.Provider provider) {
                            Mob mob = holder instanceof Mob m ? m : null;
                            assert mob != null;
                            UmweltEngine engine = new UmweltEngine(mob);

                            if (tag instanceof CompoundTag compound) {
                                engine.deserializeNBT(provider, compound);
                            }
                            return engine;
                        }

                        @Override
                        public @Nullable Tag write(@NotNull UmweltEngine engine, @NotNull HolderLookup.Provider provider) {
                            return engine.serializeNBT(provider);
                        }
                    })
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}