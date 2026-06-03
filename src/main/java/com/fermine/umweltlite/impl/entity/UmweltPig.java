package com.fermine.umweltlite.impl.entity;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.api.entity.goal.*;
import com.fermine.umweltlite.api.entity.util.UmweltGoalAPI;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fermine.umweltlite.processor.ProcessorLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class UmweltPig extends Pig implements IUmweltEntity {

    private static final EntityDataAccessor<Boolean> IS_RESTING = SynchedEntityData.defineId(UmweltPig.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_PROCESSING = SynchedEntityData.defineId(UmweltPig.class, EntityDataSerializers.BOOLEAN);

    public UmweltPig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_RESTING, false);
        builder.define(IS_PROCESSING, false);
    }

    @Override
    public UmweltEngine getUmweltEngine() {
        return this.getData(PerceptionRegistry.PERCEPTION);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // Standardized Lazy Initialization
        if (!this.isProcessing()) {
            UmweltEngine engine = getUmweltEngine();
            if (engine != null) {
                ProcessorLoader.initializeEngine(this, engine);
                this.setProcessing(true);
            }
        }

        // Spatial Memory Capture
        if (this.tickCount % 100 == 0) {
            KnowledgeAPI.setSpatialMemory(getUmweltEngine(), this.blockPosition(), new CompoundTag(), 0.8f);
        }
    }

    @Override
    public boolean isResting() { return this.entityData.get(IS_RESTING); }
    public void setResting(boolean resting) { this.entityData.set(IS_RESTING, resting); }

    @Override
    public boolean isProcessing() { return this.entityData.get(IS_PROCESSING); }
    @Override
    public void setProcessing(boolean active) { this.entityData.set(IS_PROCESSING, active); }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltAttackGoal(this, 1.2D, true),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.EmotionalRange.atLeast(0.6f),
                UmweltGoalAPI.EmotionalRange.atLeast(0.3f), UmweltGoalAPI.TraitRange.atLeast("bravery", 0.6f));

        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltPanicGoal(this, 1.35D),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.EmotionalRange.atLeast(0.4f),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.TraitRange.atLeast("anxiety", 0.5f));

        this.goalSelector.addGoal(2, new UmweltRestGoal(this));
        this.goalSelector.addGoal(2, new UmweltFollowParentGoal(this));

        UmweltGoalAPI.addComplexGoal(this, 3, new LookAtPlayerGoal(this, Player.class, 8.0F),
                UmweltGoalAPI.EmotionalRange.any(), new UmweltGoalAPI.EmotionalRange(0.2f, 0.5f),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.TraitRange.atMost("bravery", 0.6f));

        this.goalSelector.addGoal(5, new UmweltRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }
}