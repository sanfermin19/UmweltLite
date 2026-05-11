package com.fermine.umweltlite.engine;

import net.minecraft.world.phys.Vec3;
import java.util.Optional;

public record StorageInsert(
        Vec3 driveVector,
        float jumpUrge,
        Optional<String> animation
) {
    public static StorageInsert idle() {
        return new StorageInsert(Vec3.ZERO, 0f, Optional.empty());
    }

    /**
     * Helper to create a movement intent toward a specific world position.
     */
    public static StorageInsert move(Vec3 targetPos, Vec3 currentPos, String anim) {
        // Calculate the direction vector (Normalized)
        Vec3 direction = targetPos.subtract(currentPos).normalize();
        return new StorageInsert(direction, 0f, Optional.ofNullable(anim));
    }
}