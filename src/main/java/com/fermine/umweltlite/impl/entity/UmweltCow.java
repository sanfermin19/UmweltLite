package com.fermine.umweltlite.impl.entity;

import com.fermine.umweltlite.brain.capability.UmweltAttachments;
import com.fermine.umweltlite.brain.diagnostic.BrainDiagnosticScreen;
import com.fermine.umweltlite.brain.impl.UmweltBrain;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class UmweltCow extends Cow {

    // 1. Setup static accessors normally
    private static final EntityDataAccessor<Float> DATA_PANIC = SynchedEntityData.defineId(UmweltCow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_EXHAUSTION = SynchedEntityData.defineId(UmweltCow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> DATA_ACTIVE_ACTION = SynchedEntityData.defineId(UmweltCow.class, EntityDataSerializers.STRING);

    public UmweltCow(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }

    // FIX: Updated signature and builder logic for modern Minecraft versions
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);

        // Define values directly into the builder object
        builder.define(DATA_PANIC, 0.0F);
        builder.define(DATA_EXHAUSTION, 0.0F);
        builder.define(DATA_ACTIVE_ACTION, "idle");
    }

    @Override
    protected void registerGoals() {
        // Empty to keep it a blank slate
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.is(Items.RECOVERY_COMPASS)) {
            if (this.level().isClientSide()) {
                openDiagnosticGui();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    private void openDiagnosticGui() {
        Minecraft.getInstance().setScreen(new BrainDiagnosticScreen(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            UmweltBrain brain = this.getData(UmweltAttachments.BRAIN_ATTACHMENT);
            brain.tickActions(this);

            // Push live updates down to the client network parameters
            this.entityData.set(DATA_PANIC, brain.getAmygdala().getPanicFloater());
            this.entityData.set(DATA_EXHAUSTION, brain.getBrainStem().getExhaustion());
            this.entityData.set(DATA_ACTIVE_ACTION, brain.getLastSelectedActionId());
        }
    }

    public float getSyncedPanic() {
        return this.entityData.get(DATA_PANIC);
    }

    public float getSyncedExhaustion() {
        return this.entityData.get(DATA_EXHAUSTION);
    }

    public String getSyncedActiveAction() {
        return this.entityData.get(DATA_ACTIVE_ACTION);
    }
}