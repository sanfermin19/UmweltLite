package com.fermine.umweltlite.utils;

import com.fermine.umweltlite.engine.emotion.map.AttachmentMap;
import com.fermine.umweltlite.engine.knowledge.entry.KnowledgeEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class UmweltNBTUtils {

    /**
     * Safely stores a Vec3, ensuring no NaN or Infinite values are saved.
     */
    public static void putVec3(CompoundTag tag, String key, Vec3 vec) {
        CompoundTag vTag = new CompoundTag();
        vTag.putDouble("x", Double.isFinite(vec.x) ? vec.x : 0.0);
        vTag.putDouble("y", Double.isFinite(vec.y) ? vec.y : 0.0);
        vTag.putDouble("z", Double.isFinite(vec.z) ? vec.z : 0.0);
        tag.put(key, vTag);
    }

    /**
     * Retrieves a Vec3 from a tag.
     */
    public static Vec3 getVec3(CompoundTag tag, String key) {
        if (!tag.contains(key)) return Vec3.ZERO;
        CompoundTag vTag = tag.getCompound(key);
        return new Vec3(vTag.getDouble("x"), vTag.getDouble("y"), vTag.getDouble("z"));
    }

    /**
     * Validates that a float is a real, usable number.
     */
    public static float safeFloat(float value, float fallback) {
        return Float.isFinite(value) ? value : fallback;
    }

    /**
     * Helper to serialize a KnowledgeEntry into a tag.
     */
    public static CompoundTag saveEntry(KnowledgeEntry entry) {
        CompoundTag tag = new CompoundTag();
        tag.put("d", entry.value().copy());
        tag.putFloat("c", safeFloat(entry.confidence(), 0.0f));
        tag.putLong("t", entry.tickCreated());
        return tag;
    }

    /**
     * Helper to reconstruct a KnowledgeEntry from a tag.
     */
    public static KnowledgeEntry loadEntry(CompoundTag tag) {
        return new KnowledgeEntry(
                tag.getCompound("d").copy(),
                tag.getFloat("c"),
                tag.getLong("t")
        );
    }

    /**
     * Safely saves an AttachmentMap.
     */
    public static CompoundTag saveAttachment(AttachmentMap map) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("v", safeFloat(map.valence(), 0.0f));
        tag.putFloat("i", safeFloat(map.intensity(), 0.0f));
        tag.putFloat("b", safeFloat(map.bond(), 0.0f));
        return tag;
    }

    /**
     * Safely loads an AttachmentMap.
     */
    public static AttachmentMap loadAttachment(CompoundTag tag) {
        return new AttachmentMap(
                tag.getFloat("v"),
                tag.getFloat("i"),
                tag.getFloat("b")
        );
    }
}