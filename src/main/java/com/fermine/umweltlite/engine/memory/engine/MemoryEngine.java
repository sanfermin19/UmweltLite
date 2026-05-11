package com.fermine.umweltlite.engine.memory.engine;

import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.engine.memory.memory.Memory;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MemoryEngine {
    private final List<Memory> shortTermMemories = new ArrayList<>();

    // Limits to keep the "Lite" engine performant
    private static final int MAX_MEMORIES = 512;
    private static final long DEFAULT_LIFESPAN = 24000L; // 1 MC Day

    public void tick(Mob mob) {
        long time = mob.level().getGameTime();

        // Purge expired memories, keeping significant ones (trauma/rewards) twice as long
        shortTermMemories.removeIf(m -> {
            long limit = m.isSignificant() ? DEFAULT_LIFESPAN * 2 : DEFAULT_LIFESPAN;
            return m.isExpired(time, limit);
        });
    }

    /**
     * Records a new memory with spatial deduplication to prevent buffer bloating.
     */
    public void record(Memory newMemory) {
        String newId = newMemory.context().getString("id");

        // 1. Spatial Deduplication: Updates existing memory if at the same spot/type
        for (int i = 0; i < shortTermMemories.size(); i++) {
            Memory existing = shortTermMemories.get(i);
            if (existing.context().getString("id").equals(newId)) {
                if (existing.location().distanceToSqr(newMemory.location()) < 1.0) {
                    shortTermMemories.set(i, newMemory);
                    return;
                }
            }
        }

        // 2. FIFO Capacity Management
        if (shortTermMemories.size() >= MAX_MEMORIES) {
            shortTermMemories.removeFirst();
        }

        shortTermMemories.add(newMemory);
    }

    /**
     * Required by MemoryAPI: Removes a specific memory instance.
     */
    public void forget(Memory memory) {
        this.shortTermMemories.remove(memory);
    }

    /**
     * Finds memories based on context tags (e.g., "type" -> "wolf").
     */
    public List<Memory> query(String key, String value) {
        return shortTermMemories.stream()
                .filter(m -> m.context().getString(key).equals(value))
                .toList();
    }

    /**
     * Transfers strong short-term memories into permanent Knowledge.
     */
    public void consolidateToKnowledge(UmweltEngine engine) {
        long time = engine.getMob().level().getGameTime();

        for (Memory m : shortTermMemories) {
            float strength = m.getRetention(time, DEFAULT_LIFESPAN);

            if (strength > 0.8f || m.isSignificant()) {
                String type = m.context().getString("type");
                if (type.equals("location")) {
                    engine.getKnowledgeEngine().insertSpatial(
                            net.minecraft.core.BlockPos.containing(m.location()),
                            new com.fermine.umweltlite.engine.knowledge.entry.KnowledgeEntry(
                                    m.context(), strength, time
                            )
                    );
                }
            }
        }
    }

    /**
     * Returns the most recent memories, up to the specified amount.
     * Higher indices in the list are newer.
     */
    public List<Memory> getRecentMemories(int amount) {
        int size = shortTermMemories.size();
        if (size == 0) return List.of();

        int start = Math.max(0, size - amount);
        return new ArrayList<>(shortTermMemories.subList(start, size));
    }

    /**
     * Provides access to the internal memory list for the API.
     */
    public List<Memory> getShortTermMemories() {
        return this.shortTermMemories;
    }

    // --- NeoForge 1.21.1 Serialization ---

    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (Memory m : shortTermMemories) {
            CompoundTag mTag = new CompoundTag();
            mTag.putLong("t", m.timestamp());
            mTag.putDouble("x", m.location().x);
            mTag.putDouble("y", m.location().y);
            mTag.putDouble("z", m.location().z);
            mTag.putFloat("v", m.emotionalImpact().valence());
            mTag.putFloat("a", m.emotionalImpact().arousal());
            mTag.putFloat("e", m.emotionalImpact().energy()); // Fixed missing energy
            mTag.put("ctx", m.context());
            list.add(mTag);
        }

        tag.put("history", list);
        return tag;
    }

    public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag tag) {
        shortTermMemories.clear();
        if (!tag.contains("history")) return;

        ListTag list = tag.getList("history", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag mTag = list.getCompound(i);
            shortTermMemories.add(new Memory(
                    mTag.getLong("t"),
                    new Vec3(mTag.getDouble("x"), mTag.getDouble("y"), mTag.getDouble("z")),
                    new EmotionalMap(
                            mTag.getFloat("v"),
                            mTag.getFloat("a"),
                            mTag.contains("e") ? mTag.getFloat("e") : 1.0f // Fallback to 100% energy
                    ),
                    1.0f,
                    mTag.getCompound("ctx")
            ));
        }
    }
}