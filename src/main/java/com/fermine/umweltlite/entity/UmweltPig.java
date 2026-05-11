package com.fermine.umweltlite.entity;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.api.entity.goal.UmweltAttackGoal;
import com.fermine.umweltlite.api.entity.goal.UmweltPanicGoal;
import com.fermine.umweltlite.api.entity.goal.UmweltRestGoal;
import com.fermine.umweltlite.api.entity.util.UmweltGoalAPI;
import com.fermine.umweltlite.api.entity.goal.UmweltRandomStrollGoal;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.registry.PerceptionRegistry;
import com.fermine.umweltlite.processor.ProcessorLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class UmweltPig extends Pig implements IUmweltEntity {

    public UmweltPig(EntityType<? extends Pig> type, Level level) {
        super(type, level);
        UmweltEngine engine = getUmweltEngine();
        if (engine != null) {
            ProcessorLoader.initializeEngine(this, engine);
        }
    }

    @Override
    public UmweltEngine getUmweltEngine() {
        return this.getData(PerceptionRegistry.PERCEPTION);
    }

    @Override
    protected void customServerAiStep() {
        this.goalSelector.tick();

        if (isUmweltActive() && this.tickCount % 100 == 0) {
            KnowledgeAPI.setSpatialMemory(
                    getUmweltEngine(),
                    this.blockPosition(),
                    new CompoundTag(),
                    0.8f
            );
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltAttackGoal(this, 1.2D, true),
                UmweltGoalAPI.EmotionalRange.any(),
                UmweltGoalAPI.EmotionalRange.atLeast(0.6f),
                UmweltGoalAPI.EmotionalRange.atLeast(0.3f),
                UmweltGoalAPI.TraitRange.atLeast("bravery", 0.6f)
        );

        UmweltGoalAPI.addComplexGoal(this, 1, new UmweltPanicGoal(this, 1.35D),
                UmweltGoalAPI.EmotionalRange.any(),
                UmweltGoalAPI.EmotionalRange.atLeast(0.4f),
                UmweltGoalAPI.EmotionalRange.any(),
                UmweltGoalAPI.TraitRange.atLeast("anxiety", 0.5f)
        );

        this.goalSelector.addGoal(2, new UmweltRestGoal(this));

        UmweltGoalAPI.addComplexGoal(this, 3, new LookAtPlayerGoal(this, Player.class, 8.0F),
                UmweltGoalAPI.EmotionalRange.any(),
                new UmweltGoalAPI.EmotionalRange(0.2f, 0.5f), // Low-mid arousal
                UmweltGoalAPI.EmotionalRange.any(),
                UmweltGoalAPI.TraitRange.atMost("bravery", 0.6f) // Non-fighters only
        );

        this.goalSelector.addGoal(5, new UmweltRandomStrollGoal(this, 1.0D));

        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }
}