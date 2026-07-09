package com.fermine.umweltlite.goals.engine.emotion.engine;

import com.fermine.umweltlite.goals.engine.emotion.map.AttachmentMap;
import com.fermine.umweltlite.goals.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmotionalEngine {
    // Circumplex Affective Coordinates: Normalized values
    private float valence = 0.0f;  // -1.0 (Miserable/Hostile) to 1.0 (Joyful/Friendly)
    private float arousal = 0.0f;  //  0.0 (Comatose/Calm) to 1.0 (Hyper-Alert/Panic)
    private float energy = 1.0f;   //  0.0 (Exhausted) to 1.0 (Fully Charged)

    // Social Memory Tracking Registry
    private final Map<UUID, AttachmentMap> socialAttachments = new HashMap<>();
    private final Map<UUID, AttachmentMap> unmodifiableSocialView = Collections.unmodifiableMap(this.socialAttachments);

    // Pre-computed decay multipliers to completely bypass heavy Math.pow calls every tick
    private static final float DECAY_ACTIVE = (float) Math.pow(0.9995f, 0.5f);   // Moving: Slower adrenaline drop
    private static final float DECAY_IDLE = (float) Math.pow(0.9995f, 2.0f);     // Idle: Fast adrenaline drop

    // Core baseline configuration parameters
    private static final float HOME_BASE_DRIFT = 0.001f;
    private static final float PASSIVE_ENERGY_DRAIN = 0.0001f;

    /**
     * Ticks the emotional model state machine.
     * Updates biological impacts, decays temporary hyper-states, and tracks energy depletion.
     */
    public void tick(Mob mob) {
        float healthRatio = mob.getHealth() / Math.max(1.0f, mob.getMaxHealth());
        boolean isMoving = mob.getNavigation().isInProgress();
        float energyEfficiency = 0.5f + (this.energy * 0.5f);

        this.applyHomeostasis(healthRatio, isMoving, energyEfficiency);

        // Constant biological energy burn down
        this.energy = Math.max(0.0f, this.energy - PASSIVE_ENERGY_DRAIN);
    }

    /**
     * Drives values back to baseline resting rates using optimized flat calculations.
     */
    private void applyHomeostasis(float healthRatio, boolean isMoving, float energyEfficiency) {
        // Unify homeostatic recovery pathing to prevent emotional drift asymmetry
        float driftModifier = HOME_BASE_DRIFT * healthRatio * energyEfficiency;

        if (this.valence < 0.0f) {
            this.valence = Math.min(0.0f, this.valence + driftModifier);
        } else if (this.valence > 0.0f) {
            this.valence = Math.max(0.0f, this.valence - driftModifier);
        }

        // Adrenaline (Arousal) dispersion logic using our pre-calculated static factors
        float decayFactor = isMoving ? DECAY_ACTIVE : DECAY_IDLE;
        this.arousal = UmweltNBTUtils.safeFloat(this.arousal * decayFactor, 0.0f);

        if (this.arousal < 0.001f) {
            this.arousal = 0.0f;
        }
    }

    /**
     * Public API entry point to modify emotional states securely via external processors.
     */
    public void modifyState(float v, float a, float e) {
        this.valence = Mth.clamp(this.valence + v, -1.0f, 1.0f);
        this.arousal = Mth.clamp(this.arousal + a, 0.0f, 1.0f);
        this.energy = Mth.clamp(this.energy + e, 0.0f, 1.0f);
    }

    /**
     * Mutator method to manipulate social bonds dynamically over time.
     */
    public void setSocialAttachment(UUID targetUUID, AttachmentMap map) {
        if (targetUUID != null && map != null) {
            this.socialAttachments.put(targetUUID, map);
        }
    }

    /**
     * Removes an attachment completely (e.g., if an entity is killed or forgotten).
     */
    public void clearSocialAttachment(UUID targetUUID) {
        this.socialAttachments.remove(targetUUID);
    }

    // --- Persistence Management Layer ---

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        root.putFloat("v", this.valence);
        root.putFloat("a", this.arousal);
        root.putFloat("e", this.energy);

        ListTag socialList = new ListTag();
        this.socialAttachments.forEach((uuid, map) -> {
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

        this.socialAttachments.clear();
        if (root.contains("social", Tag.TAG_LIST)) {
            ListTag list = root.getList("social", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag tag = list.getCompound(i);
                if (tag.hasUUID("u")) {
                    this.socialAttachments.put(tag.getUUID("u"), UmweltNBTUtils.loadAttachment(tag));
                }
            }
        }
    }

    // --- High-Performance System Accessors ---

    /**
     * Generates a structural value snapshot container object.
     */
    public EmotionalMap getCurrentMap() {
        return new EmotionalMap(this.valence, this.arousal, this.energy);
    }

    /**
     * Exposes an unmodifiable view of the underlying map.
     * Completely eliminates memory layout thrashing by avoiding runtime structural copying entirely.
     */
    public Map<UUID, AttachmentMap> getSocialSnapshot() {
        return this.unmodifiableSocialView;
    }

    public float getEnergy() { return this.energy; }
    public float getValence() { return this.valence; }
    public float getArousal() { return this.arousal; }
}