package com.fermine.umweltlite.goals.engine.memory.engine;

import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.goals.engine.memory.memory.Memory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Manages the data-lifecycle, spatial deduplication, consolidation, and
 * NBT serialization workflows of an entity's short-term memory registry.
 */
public class MemoryEngine {
    private final List<Memory> shortTermMemories = new ArrayList<>();

    private static final int MAX_MEMORIES = 512;
    private static final long DEFAULT_LIFESPAN = 24000L; // 1 full Minecraft in-game day cycle

    /**
     * Handles the periodic expiration pruning of un-mitigated short-term memories.
     */
    public void tick(Mob mob) {
        if (mob == null || shortTermMemories.isEmpty()) return;

        long time = mob.level().getGameTime();

        // Purge expired memories, doubling persistence scales for marked traumatic or rewarding nodes
        shortTermMemories.removeIf(m -> {
            long limit = m.isSignificant() ? DEFAULT_LIFESPAN * 2 : DEFAULT_LIFESPAN;
            return m.isExpired(time, limit);
        });
    }

    /**
     * Records an observation entry with spatial deduplication tracking to prevent storage engine bloating.
     */
    public void record(Memory newMemory) {
        if (newMemory == null) return;

        String newId = newMemory.context().getString("id");

        // Spatial Deduplication: Collapse tracking nodes registering the same identity tags within a 1-block boundary
        if (!newId.isEmpty()) {
            int currentSize = shortTermMemories.size();
            for (int i = 0; i < currentSize; i++) {
                Memory existing = shortTermMemories.get(i);
                if (existing.matchesContext("id", newId)) {
                    if (existing.location().distanceToSqr(newMemory.location()) < 1.0) {
                        shortTermMemories.set(i, newMemory);
                        return;
                    }
                }
            }
        }

        // FIFO Capacity Enforcement
        if (shortTermMemories.size() >= MAX_MEMORIES) {
            shortTermMemories.removeFirst();
        }

        shortTermMemories.add(newMemory);
    }

    /**
     * In-place mutator algorithm for streaming bulk manipulation states across filtered memories.
     */
    public void modifyMemories(Predicate<Memory> filter, Function<Memory, Memory> modifier) {
        if (filter == null || modifier == null) return;

        int size = shortTermMemories.size();
        for (int i = 0; i < size; i++) {
            Memory memory = shortTermMemories.get(i);
            if (filter.test(memory)) {
                shortTermMemories.set(i, modifier.apply(memory));
            }
        }
    }

    /**
     * Direct structural conditional removal hook.
     */
    public void removeIf(Predicate<Memory> condition) {
        if (condition == null) return;
        shortTermMemories.removeIf(condition);
    }

    /**
     * Queries matching memories matching targeted context filters.
     */
    public List<Memory> query(String key, String value) {
        if (key == null || value == null || shortTermMemories.isEmpty()) {
            return Collections.emptyList();
        }

        return shortTermMemories.stream()
                .filter(m -> m.matchesContext(key, value))
                .toList();
    }

    /**
     * Translates high-retention short-term frames into localized permanent data blocks.
     */
    public void consolidateToKnowledge(UmweltEngine engine) {
        if (engine == null || engine.getMob() == null || shortTermMemories.isEmpty()) return;

        long time = engine.getMob().level().getGameTime();

        for (Memory m : shortTermMemories) {
            float strength = m.getRetention(time, DEFAULT_LIFESPAN);

            if (strength > 0.8f || m.isSignificant()) {
                if (m.matchesContext("type", "location")) {
                    engine.getKnowledgeEngine().insertSpatial(
                            BlockPos.containing(m.location()),
                            new com.fermine.umweltlite.goals.engine.knowledge.entry.KnowledgeEntry(
                                    m.context(), strength, time
                            )
                    );
                }
            }
        }
    }

    /**
     * Safely reads trailing chronological indices without out-of-bounds risks.
     */
    public List<Memory> getRecentMemories(int amount) {
        int size = shortTermMemories.size();
        if (size == 0 || amount <= 0) return Collections.emptyList();

        int start = Math.max(0, size - amount);
        return new ArrayList<>(shortTermMemories.subList(start, size));
    }

    /**
     * Exposes the internal collection pointer to API tracking routines.
     */
    public List<Memory> getShortTermMemories() {
        return this.shortTermMemories;
    }

    // --- NeoForge 1.21.1 Capability/Data Serialization ---

    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (Memory m : shortTermMemories) {
            CompoundTag mTag = new CompoundTag();
            mTag.putLong("t", m.timestamp());
            mTag.putDouble("x", m.location().x());
            mTag.putDouble("y", m.location().y());
            mTag.putDouble("z", m.location().z());

            if (m.emotionalImpact() != null) {
                mTag.putFloat("v", m.emotionalImpact().valence());
                mTag.putFloat("a", m.emotionalImpact().arousal());
                mTag.putFloat("e", m.emotionalImpact().energy());
            }
            mTag.putFloat("ic", m.initialConfidence());
            mTag.put("ctx", m.context());
            list.add(mTag);
        }

        tag.put("history", list);
        return tag;
    }

    public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag tag) {
        shortTermMemories.clear();
        if (tag == null || !tag.contains("history")) return;

        ListTag list = tag.getList("history", Tag.TAG_COMPOUND);
        int listSize = list.size();

        for (int i = 0; i < listSize; i++) {
            CompoundTag mTag = list.getCompound(i);

            EmotionalMap emotion = new EmotionalMap(
                    mTag.getFloat("v"),
                    mTag.getFloat("a"),
                    mTag.contains("e") ? mTag.getFloat("e") : 1.0f
            );

            shortTermMemories.add(new Memory(
                    mTag.getLong("t"),
                    new Vec3(mTag.getDouble("x"), mTag.getDouble("y"), mTag.getDouble("z")),
                    emotion,
                    mTag.contains("ic") ? mTag.getFloat("ic") : 1.0f,
                    mTag.getCompound("ctx")
            ));
        }
    }
}