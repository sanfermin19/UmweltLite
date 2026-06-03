package com.fermine.umweltlite.impl.engine;

import net.minecraft.world.phys.Vec3;
import java.util.Optional;

/**
 * An immutable payload representing an entity's physical desires and intent vectors generated during a single tick.
 */
public record StorageInsert(
        Vec3 driveVector,
        int jumpUrge,
        Optional<String> animation
) {
    private static final StorageInsert IDLE = new StorageInsert(Vec3.ZERO, 0, Optional.empty());

    /**
     * Provides a shared, allocation-free static fallback node for idle behaviors.
     */
    public static StorageInsert idle() {
        return IDLE;
    }
}