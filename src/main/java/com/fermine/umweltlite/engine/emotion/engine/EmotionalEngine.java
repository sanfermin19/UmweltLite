package com.fermine.umweltlite.engine.emotion.engine;

import com.fermine.umweltlite.engine.emotion.map.AttachmentMap;
import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmotionalEngine {
    private float valence = 0.0f;
    private float arousal = 0.0f;
    private float energy = 1.0f;

    private final Map<UUID, AttachmentMap> socialAttachments = new HashMap<>();

    public void tick(Mob mob) {
        float healthRatio = mob.getHealth() / Math.max(1.0f, mob.getMaxHealth());
        float activityFactor = mob.getNavigation().isInProgress() ? 0.5f : 2.0f;
        float energyEfficiency = 0.5f + (this.energy * 0.5f);

        this.applyDecay(healthRatio, activityFactor, energyEfficiency);

        // Passive energy drain - sheep get tired!
        this.energy = Math.max(0, this.energy - 0.0001f);
    }

    private void applyDecay(float healthRatio, float activityFactor, float energyEfficiency) {
        // Return to neutral state over time
        if (this.valence < 0) {
            this.valence = Math.min(0, this.valence + (0.001f * healthRatio * energyEfficiency));
        } else if (this.valence > 0) {
            this.valence = Math.max(0, this.valence - 0.001f);
        }

        // Adrenaline wearing off
        float effectiveDecay = (float) Math.pow(0.9995f, activityFactor);
        this.arousal = UmweltNBTUtils.safeFloat(this.arousal * effectiveDecay, 0.0f);

        if (this.arousal < 0.001f) this.arousal = 0;
    }

    public void modifyState(float v, float a, float e) {
        this.valence = Mth.clamp(this.valence + v, -1.0f, 1.0f);
        this.arousal = Mth.clamp(this.arousal + a, 0.0f, 1.0f);
        this.energy = Mth.clamp(this.energy + e, 0.0f, 1.0f);
    }

    // --- Persistence ---

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        root.putFloat("v", valence);
        root.putFloat("a", arousal);
        root.putFloat("e", energy);

        ListTag socialList = new ListTag();
        socialAttachments.forEach((uuid, map) -> {
            CompoundTag entry = UmweltNBTUtils.saveAttachment(map);
            entry.putUUID("u", uuid);
            socialList.add(entry);
        });
        root.put("social", socialList);

        return root;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag root) {
        if (root == null) return;
        this.valence = Mth.clamp(root.getFloat("v"), -1.0f, 1.0f);
        this.arousal = Mth.clamp(root.getFloat("a"), 0.0f, 1.0f);
        this.energy = Mth.clamp(root.getFloat("e"), 0.0f, 1.0f);

        socialAttachments.clear();
        if (root.contains("social")) {
            ListTag list = root.getList("social", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                socialAttachments.put(tag.getUUID("u"), UmweltNBTUtils.loadAttachment(tag));
            }
        }
    }

    // Snapshot & Getters
    public EmotionalMap getCurrentMap() { return new EmotionalMap(valence, arousal, energy); }
    public Map<UUID, AttachmentMap> getSocialSnapshot() { return Map.copyOf(socialAttachments); }
    public float getEnergy() { return energy; }
    public float getValence() { return valence; }
    public float getArousal() { return arousal; }
}