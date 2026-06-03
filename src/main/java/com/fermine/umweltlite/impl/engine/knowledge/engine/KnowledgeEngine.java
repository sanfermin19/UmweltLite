package com.fermine.umweltlite.impl.engine.knowledge.engine;

import com.fermine.umweltlite.impl.engine.knowledge.entry.KnowledgeEntry;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class KnowledgeEngine {
    private final Map<BlockPos, KnowledgeEntry> spatialMap = new HashMap<>();
    private final Map<String, KnowledgeEntry> factMap = new HashMap<>();

    private final Map<BlockPos, KnowledgeEntry> unmodifiableSpatialView = Collections.unmodifiableMap(this.spatialMap);
    private final Map<String, KnowledgeEntry> unmodifiableFactView = Collections.unmodifiableMap(this.factMap);

    /**
     * Managed heartbeat tick handling age-based mental degradation and sizing thresholds.
     * Scheduled on a 10-second interval modulo to radically drop system load.
     */
    public void tick(long gameTime) {
        if (gameTime % 200 == 0) {
            // 1. Passive fading execution
            this.spatialMap.entrySet().removeIf(e -> e.getValue().isStale(gameTime, 12000L));
            this.factMap.entrySet().removeIf(e -> e.getValue().isStale(gameTime, 48000L));

            // 2. Hard Cap Safety (Prevent RAM Bloat)
            if (this.spatialMap.size() > 500) {
                this.pruneOldest(this.spatialMap, 400);
            }
            if (this.factMap.size() > 1000) {
                this.pruneOldest(this.factMap, 800);
            }
        }
    }

    /**
     * Shrinks internal tracking maps down to target capacities using an optimized collection sort.
     */
    private <K> void pruneOldest(Map<K, KnowledgeEntry> map, int targetSize) {
        int itemsToPrune = map.size() - targetSize;
        if (itemsToPrune <= 0) return;

        List<Map.Entry<K, KnowledgeEntry>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparingLong(e -> e.getValue().tickCreated()));

        for (int i = 0; i < itemsToPrune; i++) {
            map.remove(entries.get(i).getKey());
        }
    }

    /**
     * Runs deep qualitative filtering across all mental tables to purge garbage inputs or corrupted math values.
     */
    public void performDeepPruning() {
        this.spatialMap.entrySet().removeIf(entry ->
                entry.getValue().confidence() < 0.3f || !entry.getValue().isDataValid());

        this.factMap.entrySet().removeIf(entry ->
                entry.getValue().confidence() < 0.2f || !entry.getValue().isDataValid());
    }

    // --- API Internal Mutators ---

    public void insertSpatial(BlockPos pos, KnowledgeEntry entry) {
        if (pos != null && entry != null && entry.isDataValid()) {
            this.spatialMap.put(pos.immutable(), entry);
        }
    }

    public void insertFact(String key, KnowledgeEntry entry) {
        if (key != null && entry != null && entry.isDataValid()) {
            this.factMap.put(key, entry);
        }
    }

    public void removeSpatial(BlockPos pos) {
        if (pos != null) this.spatialMap.remove(pos);
    }

    public void removeFact(String key) {
        if (key != null) this.factMap.remove(key);
    }

    public void clearAllMemoryChannels() {
        this.spatialMap.clear();
        this.factMap.clear();
    }

    public Optional<KnowledgeEntry> getSpatial(BlockPos pos) {
        if (pos == null) return Optional.empty();
        return Optional.ofNullable(this.spatialMap.get(pos));
    }

    public Optional<KnowledgeEntry> getFact(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(this.factMap.get(key));
    }

    // High performance allocation-free unmodifiable entry windows
    public Map<BlockPos, KnowledgeEntry> getSpatialMap() { return this.unmodifiableSpatialView; }
    public Map<String, KnowledgeEntry> getFactMap() { return this.unmodifiableFactView; }

    // --- Persistence Configuration ---

    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();

        ListTag spatialList = new ListTag();
        this.spatialMap.forEach((pos, entry) -> {
            CompoundTag entryTag = UmweltNBTUtils.saveEntry(entry);
            entryTag.putLong("p", pos.asLong());
            spatialList.add(entryTag);
        });
        root.put("spatial", spatialList);

        ListTag factList = new ListTag();
        this.factMap.forEach((id, entry) -> {
            CompoundTag entryTag = UmweltNBTUtils.saveEntry(entry);
            entryTag.putString("id", id);
            factList.add(entryTag);
        });
        root.put("facts", factList);

        return root;
    }

    public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag root) {
        this.clearAllMemoryChannels();
        if (root == null) return;

        // Swapped to explicit ID type checked tag checks to safeguard registry lookups
        if (root.contains("spatial", Tag.TAG_LIST)) {
            ListTag list = root.getList("spatial", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                if (tag.contains("p", Tag.TAG_LONG)) {
                    this.spatialMap.put(
                            BlockPos.of(tag.getLong("p")),
                            UmweltNBTUtils.loadEntry(tag)
                    );
                }
            }
        }

        if (root.contains("facts", Tag.TAG_LIST)) {
            ListTag list = root.getList("facts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                if (tag.contains("id", Tag.TAG_STRING)) {
                    this.factMap.put(
                            tag.getString("id"),
                            UmweltNBTUtils.loadEntry(tag)
                    );
                }
            }
        }
    }
}