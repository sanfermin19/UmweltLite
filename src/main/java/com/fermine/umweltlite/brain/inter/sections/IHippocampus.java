package com.fermine.umweltlite.brain.inter.sections;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.core.BlockPos;
import java.util.Optional;
import java.util.UUID;

public interface IHippocampus extends IBrainComponent {
    void recordTrauma(UUID entityId, float traumaWeight);
    void archiveSafeZone(BlockPos pos);
    float getTraumaLevel(UUID entityId);
    Optional<BlockPos> getStableSafeZone();
}