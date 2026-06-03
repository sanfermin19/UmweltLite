package com.fermine.umweltlite.brain.impl.sections;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Hippocampus implements IBrainComponent {

    // Memory Storage Units
    private final Map<UUID, Float> traumaRegistry = new HashMap<>();
    private BlockPos stableSafeZone = null;

    // Memory Decay Tracker
    private int ticksSinceLastTrauma = 0;

    @Override
    public void tick(LivingEntity host) {
        this.ticksSinceLastTrauma++;

        // Passive Neurological Decay: Very minor trauma fades over thousands of ticks if completely undisturbed
        if (this.ticksSinceLastTrauma > 1200 && !traumaRegistry.isEmpty()) {
            // Iterative decay formula to prevent concurrent modification issues
            this.traumaRegistry.replaceAll((uuid, weight) -> Math.max(0.0f, weight - 0.0001f));
            this.traumaRegistry.values().removeIf(weight -> weight <= 0.005f);
            this.ticksSinceLastTrauma = 1200; // Cap boundary
        }
    }

    /**
     * Records or amplifies a traumatic memory associated with a specific entity.
     * High trauma weights alter the appraisal threshold of the organism permanently.
     */
    public void recordTrauma(UUID entityId, float traumaWeight) {
        float currentWeight = this.traumaRegistry.getOrDefault(entityId, 0.0f);
        // Diminishing returns formula to represent psychological saturation
        float newWeight = Math.min(1.0f, currentWeight + (traumaWeight * (1.0f - currentWeight)));

        this.traumaRegistry.put(entityId, newWeight);
        this.ticksSinceLastTrauma = 0; // Reset decay counter upon active re-traumatization
    }

    public void archiveSafeZone(BlockPos pos) {
        this.stableSafeZone = pos.immutable();
    }

    @Override
    public void reset() {
        this.traumaRegistry.clear();
        this.stableSafeZone = null;
        this.ticksSinceLastTrauma = 0;
    }

    // Getters
    public float getTraumaLevel(UUID entityId) {
        return this.traumaRegistry.getOrDefault(entityId, 0.0f);
    }

    public Optional<BlockPos> getStableSafeZone() {
        return Optional.ofNullable(this.stableSafeZone);
    }

    public Map<UUID, Float> getFullTraumaRegistry() {
        return this.traumaRegistry;
    }

    // --- Serialization Engines for NBT Continuity ---

    public CompoundTag saveMemoryData() {
        CompoundTag memoryTag = new CompoundTag();
        ListTag list = new ListTag();

        for (Map.Entry<UUID, Float> entry : traumaRegistry.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("TargetUUID", entry.getKey());
            entryTag.putFloat("TraumaWeight", entry.getValue());
            list.add(entryTag);
        }

        memoryTag.put("TraumaRegistry", list);

        if (this.stableSafeZone != null) {
            memoryTag.putLong("SafeZonePos", this.stableSafeZone.asLong());
        }

        return memoryTag;
    }

    public void loadMemoryData(CompoundTag memoryTag) {
        this.traumaRegistry.clear();

        if (memoryTag.contains("TraumaRegistry", Tag.TAG_LIST)) {
            ListTag list = memoryTag.getList("TraumaRegistry", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                this.traumaRegistry.put(entryTag.getUUID("TargetUUID"), entryTag.getFloat("TraumaWeight"));
            }
        }

        if (memoryTag.contains("SafeZonePos")) {
            this.stableSafeZone = BlockPos.of(memoryTag.getLong("SafeZonePos"));
        }
    }
}