package com.fermine.umweltlite.brain.capability;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.brain.impl.UmweltBrain;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class UmweltAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, UmweltLite.MODID);

    // Register an attachment type that knows how to create a brain and serialize its NBT data
    public static final Supplier<AttachmentType<UmweltBrain>> BRAIN_ATTACHMENT = ATTACHMENT_TYPES.register(
            "brain",
            () -> AttachmentType.builder(UmweltBrain::new)
                    // Pass a modern NeoForge IAttachmentSerializer implementation directly
                    .serialize(new IAttachmentSerializer<CompoundTag, UmweltBrain>() {
                        @Override
                        public @NotNull UmweltBrain read(net.neoforged.neoforge.attachment.@NotNull IAttachmentHolder holder, @NotNull CompoundTag nbt, net.minecraft.core.HolderLookup.@NotNull Provider provider) {
                            // future implementation hook: brain.loadFromNBT(nbt);
                            return new UmweltBrain();
                        }

                        @Nullable
                        @Override
                        public CompoundTag write(@NotNull UmweltBrain brain, net.minecraft.core.HolderLookup.@NotNull Provider provider) {
                            // future implementation hook: brain.saveToNBT(nbt);
                            return new CompoundTag();
                        }
                    })
                    .build()
    );
}