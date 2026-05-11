package com.fermine.umweltlite.engine.knowledge.engine;

import com.fermine.umweltlite.engine.knowledge.entry.KnowledgeEntry;
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

    public void tick(long gameTime) {
        if (gameTime % 200 == 0) {
            // 1. Passive fading
            spatialMap.entrySet().removeIf(e -> e.getValue().isStale(gameTime, 12000));
            factMap.entrySet().removeIf(e -> e.getValue().isStale(gameTime, 48000));

            // 2. Hard Cap Safety (Prevent RAM Bloat)
            if (spatialMap.size() > 500) pruneOldest(spatialMap, 400);
            if (factMap.size() > 1000) pruneOldest(factMap, 800);
        }
    }

    private <K> void pruneOldest(Map<K, KnowledgeEntry> map, int targetSize) {
        List<Map.Entry<K, KnowledgeEntry>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparingLong(e -> e.getValue().tickCreated()));
        for (int i = 0; i < entries.size() - targetSize; i++) {
            map.remove(entries.get(i).getKey());
        }
    }

    public void performDeepPruning(long currentTick) {
        // Prune low confidence or corrupted data
        spatialMap.entrySet().removeIf(entry ->
                entry.getValue().confidence() < 0.3f || !entry.getValue().isDataValid());

        factMap.entrySet().removeIf(entry ->
                entry.getValue().confidence() < 0.2f || !entry.getValue().isDataValid());
    }

    // --- API & Getters ---

    public void insertSpatial(BlockPos pos, KnowledgeEntry entry) {
        if (entry.isDataValid()) spatialMap.put(pos.immutable(), entry);
    }

    public void insertFact(String key, KnowledgeEntry entry) {
        if (entry.isDataValid()) factMap.put(key, entry);
    }

    public void removeSpatial(BlockPos pos) { spatialMap.remove(pos); }
    public void removeFact(String key) { factMap.remove(key); }

    public Optional<KnowledgeEntry> getSpatial(BlockPos pos) {
        return Optional.ofNullable(spatialMap.get(pos));
    }

    public Optional<KnowledgeEntry> getFact(String key) {
        return Optional.ofNullable(factMap.get(key));
    }

    public Map<BlockPos, KnowledgeEntry> getSpatialMap() { return Collections.unmodifiableMap(this.spatialMap); }
    public Map<String, KnowledgeEntry> getFactMap() { return Collections.unmodifiableMap(this.factMap); }

    // --- Persistence ---

    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();

        ListTag spatialList = new ListTag();
        spatialMap.forEach((pos, entry) -> {
            CompoundTag entryTag = UmweltNBTUtils.saveEntry(entry);
            entryTag.putLong("p", pos.asLong()); // BlockPos is specific to the map key
            spatialList.add(entryTag);
        });
        root.put("spatial", spatialList);

        ListTag factList = new ListTag();
        factMap.forEach((id, entry) -> {
            CompoundTag entryTag = UmweltNBTUtils.saveEntry(entry);
            entryTag.putString("id", id); // String ID is specific to the map key
            factList.add(entryTag);
        });
        root.put("facts", factList);

        return root;
    }

    public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag root) {
        spatialMap.clear();
        factMap.clear();

        if (root.contains("spatial")) {
            ListTag list = root.getList("spatial", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                spatialMap.put(
                        BlockPos.of(tag.getLong("p")),
                        UmweltNBTUtils.loadEntry(tag)
                );
            }
        }

        if (root.contains("facts")) {
            ListTag list = root.getList("facts", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                factMap.put(
                        tag.getString("id"),
                        UmweltNBTUtils.loadEntry(tag)
                );
            }
        }
    }
}