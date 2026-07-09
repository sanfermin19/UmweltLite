package com.fermine.umweltlite.utils;

import com.fermine.umweltlite.goals.engine.emotion.map.AttachmentMap;
import com.fermine.umweltlite.goals.engine.knowledge.entry.KnowledgeEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public class UmweltNBTUtils {

    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";

    private static final String DATA_KEY = "d";
    private static final String CONFIDENCE_KEY = "c";
    private static final String TIMESTAMP_KEY = "t";

    private static final String VALENCE_KEY = "v";
    private static final String AROUSAL_KEY = "a";
    private static final String BOND_KEY = "b";

    /**
     * Stores a Vec3 vector safely into NBT compounds without running risk of coordinate corruption.
     */
    public static void putVec3(CompoundTag tag, String key, Vec3 vec) {
        if (tag == null || vec == null) return;

        CompoundTag vTag = new CompoundTag();
        vTag.putDouble(X_KEY, Double.isFinite(vec.x) ? vec.x : 0.0);
        vTag.putDouble(Y_KEY, Double.isFinite(vec.y) ? vec.y : 0.0);
        vTag.putDouble(Z_KEY, Double.isFinite(vec.z) ? vec.z : 0.0);
        tag.put(key, vTag);
    }

    /**
     * Pulls structural Vec3 coordinates from data compounds with a guaranteed zero fallback state.
     */
    public static Vec3 getVec3(CompoundTag tag, String key) {
        if (tag == null || !tag.contains(key, Tag.TAG_COMPOUND)) return Vec3.ZERO;
        CompoundTag vTag = tag.getCompound(key);
        return new Vec3(vTag.getDouble(X_KEY), vTag.getDouble(Y_KEY), vTag.getDouble(Z_KEY));
    }

    /**
     * Structural fallback intercept preventing propagation of broken floats across components.
     */
    public static float safeFloat(float value, float fallback) {
        return Float.isFinite(value) ? value : fallback;
    }

    /**
     * Flattens concrete game knowledge states down into data packages securely.
     */
    public static CompoundTag saveEntry(KnowledgeEntry entry) {
        CompoundTag tag = new CompoundTag();
        if (entry == null) return tag;

        // Deep copy nested data maps to keep the snapshot decoupled from runtime object modification
        tag.put(DATA_KEY, entry.value().copy());
        tag.putFloat(CONFIDENCE_KEY, safeFloat(entry.confidence(), 0.0f));
        tag.putLong(TIMESTAMP_KEY, entry.tickCreated());
        return tag;
    }

    /**
     * Inflates historical structural snapshots directly back up into live Memory objects.
     */
    public static KnowledgeEntry loadEntry(CompoundTag tag) {
        if (tag == null) {
            return new KnowledgeEntry(new CompoundTag(), 0.0f, 0L);
        }
        return new KnowledgeEntry(
                tag.getCompound(DATA_KEY).copy(),
                tag.getFloat(CONFIDENCE_KEY),
                tag.getLong(TIMESTAMP_KEY)
        );
    }

    /**
     * Compresses active relative alignment mappings into data tags.
     */
    public static CompoundTag saveAttachment(AttachmentMap map) {
        CompoundTag tag = new CompoundTag();
        if (map == null) return tag;

        tag.putFloat(VALENCE_KEY, safeFloat(map.valence(), 0.0f));
        tag.putFloat(AROUSAL_KEY, safeFloat(map.arousal(), 0.0f));
        tag.putFloat(BOND_KEY, safeFloat(map.bond(), 0.0f));
        return tag;
    }

    /**
     * Unwraps structured data entries cleanly into finalized relationship bounds records.
     */
    public static AttachmentMap loadAttachment(CompoundTag tag) {
        if (tag == null) {
            return new AttachmentMap(0.0f, 0.0f, 0.0f);
        }
        return new AttachmentMap(
                tag.getFloat(VALENCE_KEY),
                tag.getFloat(AROUSAL_KEY),
                tag.getFloat(BOND_KEY)
        );
    }
}