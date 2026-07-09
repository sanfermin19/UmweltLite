package com.fermine.umweltlite.example.entity.goals;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.api.entity.goal.*;
import com.fermine.umweltlite.api.entity.util.UmweltGoalAPI;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fermine.umweltlite.processor.ProcessorLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class UmweltSheep extends Sheep implements IUmweltEntity {

    private static final EntityDataAccessor<Boolean> IS_RESTING = SynchedEntityData.defineId(UmweltSheep.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_PROCESSING = SynchedEntityData.defineId(UmweltSheep.class, EntityDataSerializers.BOOLEAN);

    public UmweltSheep(EntityType<? extends Sheep> type, Level level) {
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
        super.customServerAiStep(); // Now safe because EatBlockGoal is initialized!

        if (!this.isProcessing()) {
            UmweltEngine engine = getUmweltEngine();
            if (engine != null) {
                ProcessorLoader.initializeEngine(this, engine);
                this.setProcessing(true);
            }
        }

        if (this.tickCount % 100 == 0) {
            KnowledgeAPI.setSpatialMemory(getUmweltEngine(), this.blockPosition(), new CompoundTag(), 0.8f);
        }
    }

    @Override
    public boolean isResting() { return this.entityData.get(IS_RESTING); }
    @Override
    public boolean isProcessing() { return this.entityData.get(IS_PROCESSING); }
    @Override
    public void setProcessing(boolean active) { this.entityData.set(IS_PROCESSING, active); }

    @Override
    protected void registerGoals() {
        // IMPORTANT: Call super first to register vanilla goals and initialize internal fields
        // like eatBlockGoal, swimGoal, etc.
        super.registerGoals();

        // Now we can add our custom Umwelte-driven behavior on top of the vanilla ones
        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltAttackGoal(this, 1.2D, true),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.EmotionalRange.atLeast(0.5f),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.TraitRange.atLeast("bravery", 0.7f));

        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltPanicGoal(this, 1.5D),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.EmotionalRange.atLeast(0.3f),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.TraitRange.atLeast("anxiety", 0.5f));

        UmweltGoalAPI.addComplexGoal(this, 4, new LookAtPlayerGoal(this, Player.class, 8.0F),
                UmweltGoalAPI.EmotionalRange.atLeast(-0.2f), UmweltGoalAPI.EmotionalRange.atMost(0.4f),
                UmweltGoalAPI.EmotionalRange.any(), UmweltGoalAPI.TraitRange.atMost("anxiety", 0.4f));


        this.goalSelector.getAvailableGoals().removeIf(goal -> goal.getGoal() instanceof RandomStrollGoal);
        this.goalSelector.addGoal(5, new UmweltRandomStrollGoal(this, 1.0D));
    }
}