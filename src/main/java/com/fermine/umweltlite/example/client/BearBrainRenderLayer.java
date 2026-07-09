package com.fermine.umweltlite.example.client;

import com.fermine.umweltlite.brain.capability.UmweltAttachments;
import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

public class BearBrainRenderLayer extends RenderLayer<PolarBear, PolarBearModel<PolarBear>> {

    private static final ResourceLocation BRAIN_GLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("umweltlite", "textures/entity/polarbear/bear_brain_glow.png");

    public BearBrainRenderLayer(RenderLayerParent<PolarBear, PolarBearModel<PolarBear>> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight,
                       @NotNull PolarBear bear, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        if (bear.hasData(UmweltAttachments.BRAIN_PIPELINE)) {
            IUmweltBrainPipeline pipeline = bear.getData(UmweltAttachments.BRAIN_PIPELINE);

            // Map Triad drives beautifully to RGB spectrum glows
            int r = (int) (pipeline.getSurvival() * 255.0F);
            int g = (int) (pipeline.getAnalytical() * 255.0F);
            int b = (int) (pipeline.getSelfInterest() * 255.0F);
            int a = (int) (0.7F * 255.0F);

            int packedColor = FastColor.ARGB32.color(a, r, g, b);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(BRAIN_GLOW_TEXTURE));
            int overlayCoords = net.minecraft.client.renderer.entity.LivingEntityRenderer.getOverlayCoords(bear, 0.0F);

            this.getParentModel().renderToBuffer(poseStack, vertexConsumer, packedLight, overlayCoords, packedColor);
        }
    }
}