package com.fermine.umweltlite.brain.inter.lobes;

import com.fermine.umweltlite.brain.inter.IBrainComponent;
import java.util.Optional;
import java.util.UUID;

public interface IOccipitalLobe extends IBrainComponent {
    Optional<UUID> getFocusedTargetUUID();
    float getVisualAcuity();
    boolean isTargetInFocus();
}