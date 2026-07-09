package com.fermine.umweltlite.brain.capability;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Centralized NeoForge Attachment registry for the Cognitive Triad architecture.
 */
public class UmweltAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, UmweltLite.MODID);

    /**
     * Polymorphic Cognitive Pipeline data slot.
     * To prevent initialization failures, defaults to an empty, non-op block if read raw,
     * but concrete implementations are mapped at the entity layer.
     */
    public static final Supplier<AttachmentType<IUmweltBrainPipeline>> BRAIN_PIPELINE = ATTACHMENT_TYPES.register(
            "brain_pipeline",
            () -> AttachmentType.builder(() -> (IUmweltBrainPipeline) null) // Dynamic programmatic attachment mapping
                    .serialize(new IAttachmentSerializer<CompoundTag, IUmweltBrainPipeline>() {
                        @Override
                        public @NotNull IUmweltBrainPipeline read(net.neoforged.neoforge.attachment.@NotNull IAttachmentHolder holder, @NotNull CompoundTag nbt, net.minecraft.core.HolderLookup.@NotNull Provider provider) {
                            String typeId = nbt.getString("BrainType");
                            IUmweltBrainPipeline pipeline = UmweltBrainRegistry.createPipelineInstance(ResourceLocation.parse(typeId));
                            if (pipeline != null) {
                                pipeline.deserializePipeline(nbt);
                            }
                            return pipeline;
                        }

                        @Override
                        public CompoundTag write(@NotNull IUmweltBrainPipeline pipeline, net.minecraft.core.HolderLookup.@NotNull Provider provider) {
                            CompoundTag tag = pipeline.serializePipeline();
                            // Safeguard type serialization identifier to reconstruct polymorphic subclasses safely
                            tag.putString("BrainType", pipeline.getBrainTypeId().toString());
                            return tag;
                        }
                    })
                    .build()
    );
}