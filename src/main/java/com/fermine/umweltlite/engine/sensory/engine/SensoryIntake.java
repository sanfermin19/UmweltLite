package com.fermine.umweltlite.engine.sensory.engine;

import com.fermine.umweltlite.engine.sensory.perception.BlockPerception;
import com.fermine.umweltlite.engine.sensory.perception.EntityPerception;
import com.fermine.umweltlite.engine.sensory.perception.WorldPerception;
import com.fermine.umweltlite.engine.sensory.raycast.RaycastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensoryIntake {
    // Now tracking observations instead of just raw signals
    public final List<BlockObservation> blocks = new ArrayList<>();
    public final List<EntityObservation> entities = new ArrayList<>();

    private final Map<String, RaycastResult> spatialRays = new HashMap<>();
    public WorldPerception worldState = WorldPerception.EMPTY;

    public void clear() {
        this.blocks.clear();
        this.entities.clear();
        this.spatialRays.clear();
        this.worldState = WorldPerception.EMPTY;
    }

    // --- Helper Methods for Senses ---

    public void addEntity(LivingEntity entity, EntityPerception perception, float intensity, String senseType) {
        if (entity != null && perception != null) {
            this.entities.add(new EntityObservation(entity, perception, intensity, senseType));
        }
    }

    public void setRay(String name, RaycastResult result) {
        if (result.isValid()) {
            this.spatialRays.put(name, result);
        }
    }

    public RaycastResult getRay(String name) {
        return spatialRays.getOrDefault(name, RaycastResult.EMPTY);
    }

    // --- Records ---

    public record EntityObservation(
            LivingEntity entity,
            EntityPerception perception,
            float intensity,
            String senseType // "audio", "visual", etc.
    ) {}

    public record BlockObservation(
            BlockPos pos,
            BlockState state,
            BlockPerception perception
    ) {}
}