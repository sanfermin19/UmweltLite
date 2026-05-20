package com.fermine.umweltlite.engine.sensory.analyzer;

import com.fermine.umweltlite.engine.sensory.perception.BlockPerception;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;

public final class BlockAnalyzer {
    // Caching is based on the Block, not the State, to prevent memory leaks from properties like rotation.
    private static final ConcurrentHashMap<Block, BlockPerception> KNOWLEDGE_BASE = new ConcurrentHashMap<>();

    public static BlockPerception analyze(BlockState state) {
        if (state == null || state.isAir()) {
            return new BlockPerception(0.0f, 0.0f, 0.0f, 0.0f);
        }

        // Standardize to the base block to keep the map size small.
        return KNOWLEDGE_BASE.computeIfAbsent(state.getBlock(), b -> calculatePerception(state));
    }

    private static BlockPerception calculatePerception(BlockState state) {
        // 1. DATA INTAKE (Using BlockPos.ZERO as a dummy since the process is context-independent)
        int colorRGB = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).col;
        float hardness = state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        var sound = state.getSoundType();

        // 2. VISUAL PROCESSING
        float r = ((colorRGB >> 16) & 0xFF) / 255.0f;
        float g = ((colorRGB >> 8) & 0xFF) / 255.0f;
        float b = (colorRGB & 0xFF) / 255.0f;

        // Using standard luminance coefficients
        float intensity = (0.299f * r + 0.587f * g + 0.114f * b);
        float saturation = (Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)));

        // 3. PHYSICAL PROCESSING
        // Guard against division by zero in pitch
        float pitch = Math.max(sound.getPitch(), 0.1f);
        float roughness = Mth.clamp(sound.getVolume() / pitch, 0.0f, 1.0f);

        // Visual Noise: Harder or more saturated blocks are treated as "complex".
        float visualNoise = Mth.clamp((hardness / 10.0f) + (saturation * 0.5f), 0.0f, 1.0f);

        // 4. FINAL SAFETY WRAP
        return new BlockPerception(
                UmweltNBTUtils.safeFloat(visualNoise, 0.0f),
                UmweltNBTUtils.safeFloat(intensity, 0.0f),
                UmweltNBTUtils.safeFloat(saturation, 0.0f),
                UmweltNBTUtils.safeFloat(roughness, 0.0f)
        );
    }

    private BlockAnalyzer() {}
}