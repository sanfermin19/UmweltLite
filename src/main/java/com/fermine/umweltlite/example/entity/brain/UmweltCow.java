package com.fermine.umweltlite.example.entity.brain;

import com.fermine.umweltlite.brain.cognitive.ICognitiveOrganism;
import com.fermine.umweltlite.brain.capability.UmweltAttachments;
import com.fermine.umweltlite.brain.diagnostic.BrainDiagnosticLogger;
import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UmweltCow extends Cow implements ICognitiveOrganism {

    public UmweltCow(EntityType<? extends Cow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        // Handing complete baseline steering control over to our cognitive engine actions
    }

    @Override
    public boolean isCognitiveEngineActive() {
        if (!this.hasData(UmweltAttachments.BRAIN_PIPELINE)) return false;
        this.getData(UmweltAttachments.BRAIN_PIPELINE);
        return true;
    }

    @Override
    public @Nullable IUmweltBrainPipeline getCognitivePipeline() {
        return this.getData(UmweltAttachments.BRAIN_PIPELINE);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Click the cow with a Recovery Compass to print out the structural neural diagnostics directly to chat!
        if (itemstack.is(Items.RECOVERY_COMPASS)) {
            if (!this.level().isClientSide()) {
                BrainDiagnosticLogger.printDiagnosticsToChat(player, this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }
}