package com.fermine.umweltlite.goals.engine.sensory.engine;

import com.fermine.umweltlite.goals.engine.sensory.perception.EntityPerception;
import com.fermine.umweltlite.goals.engine.sensory.perception.BlockPerception;
import com.fermine.umweltlite.goals.engine.sensory.perception.WorldPerception;
import com.fermine.umweltlite.goals.engine.sensory.raycast.RaycastResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance, low-allocation zero-copy sensory buffer.
 * Holds objective environment states before they are processed by cognitive engines.
 */
public class SensoryIntake {
    private Mob hostMob;
    public WorldPerception worldState = WorldPerception.EMPTY;

    public final List<EntityObservation> entities = new ArrayList<>();
    public final List<BlockObservation> blocks = new ArrayList<>();
    private final Map<String, RaycastResult> rayRegistry = new ConcurrentHashMap<>();

    public void beginTick(Mob mob) {
        this.hostMob = mob;
        this.clear();
    }

    public void clear() {
        this.entities.clear();
        this.blocks.clear();
        this.rayRegistry.clear();
        this.worldState = WorldPerception.EMPTY;
    }

    public Mob mobRef() {
        return this.hostMob;
    }

    public void setRay(String identity, RaycastResult result) {
        if (result == null) {
            this.rayRegistry.remove(identity);
        } else {
            this.rayRegistry.put(identity, result);
        }
    }

    public RaycastResult getRay(String identity) {
        return this.rayRegistry.getOrDefault(identity, RaycastResult.EMPTY);
    }

    // --- INNER OBSERVATION RECORDS ---
    public record EntityObservation(
            LivingEntity entity,
            EntityPerception perception,
            float intensity,
            String senseType
    ) {}

    public record BlockObservation(
            BlockPos pos,
            BlockState state,
            BlockPerception perception
    ) {}
}