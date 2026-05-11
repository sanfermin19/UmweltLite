package com.fermine.umweltlite.engine.sensory.raycast;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public record RaycastResult(
        Vec3 hitLocation,
        Direction sideHit,
        BlockPos blockPos,
        boolean isEntity,
        Optional<UUID> entityId,
        boolean isMiss // Explicit flag for failure
) {
    public static final RaycastResult EMPTY = new RaycastResult(
            Vec3.ZERO, Direction.UP, null, false, Optional.empty(), true
    );

    /**
     * Hardened check: Ensures the ray hit something AND the math isn't corrupted.
     */
    public boolean isValid() {
        return !isMiss &&
                blockPos != null &&
                Double.isFinite(hitLocation.x) &&
                Double.isFinite(hitLocation.y) &&
                Double.isFinite(hitLocation.z);
    }

    public boolean isBlocked() {
        return isValid();
    }
}