package com.fermine.umweltlite.goals.engine.sensory.analyzer;

import com.fermine.umweltlite.goals.engine.sensory.perception.BlockPerception;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockAnalyzer {
    private static final ConcurrentHashMap<Block, BlockPerception> KNOWLEDGE_BASE = new ConcurrentHashMap<>();

    public static BlockPerception analyze(BlockState state) {
        if (state == null || state.isAir()) {
            return BlockPerception.EMPTY;
        }

        return KNOWLEDGE_BASE.computeIfAbsent(state.getBlock(), b -> calculatePerception(state));
    }

    private static BlockPerception calculatePerception(BlockState state) {
        // 1. DATA INTAKE
        int colorRGB = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).col;
        float hardness = state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var sound = state.getSoundType();

        // 2. VISUAL PROCESSING
        float r = ((colorRGB >> 16) & 0xFF) / 255.0f;
        float g = ((colorRGB >> 8) & 0xFF) / 255.0f;
        float b = (colorRGB & 0xFF) / 255.0f;

        float intensity = (0.299f * r + 0.587f * g + 0.114f * b);
        float saturation = (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)));

        // 3. PHYSICAL PROCESSING
        float pitch = Math.max(sound.getPitch(), 0.1f);
        float roughness = Mth.clamp(sound.getVolume() / pitch, 0.0f, 1.0f);
        float visualNoise = Mth.clamp((hardness / 10.0f) + (saturation * 0.5f), 0.0f, 1.0f);

        // 4. OBJECTIVE HAZARD/INTEREST RATINGS
        float dangerRating = 0.0f;
        if (state.is(BlockTags.FIRE) || state.is(BlockTags.CAMPFIRES)) {
            dangerRating = 0.9f;
        } else if (hardness > 49.0f) {
            dangerRating = 0.5f;
        }

        float interestRating = Mth.clamp((saturation * 0.6f) + (roughness * 0.4f), 0.0f, 1.0f);

        // 5. FINAL WRAP
        return new BlockPerception(
                UmweltNBTUtils.safeFloat(visualNoise, 0.0f),
                UmweltNBTUtils.safeFloat(intensity, 0.0f),
                UmweltNBTUtils.safeFloat(saturation, 0.0f),
                UmweltNBTUtils.safeFloat(roughness, 0.0f),
                UmweltNBTUtils.safeFloat(dangerRating, 0.0f),
                UmweltNBTUtils.safeFloat(interestRating, 0.0f)
        );
    }

    private BlockAnalyzer() {}
}