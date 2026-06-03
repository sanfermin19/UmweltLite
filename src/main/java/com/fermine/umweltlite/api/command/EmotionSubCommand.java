package com.fermine.umweltlite.api.command;

import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Optional;

public class EmotionSubCommand {

    private static final SimpleCommandExceptionType ERROR_NOT_UMWELT_COMPATIBLE = new SimpleCommandExceptionType(
            Component.literal("None of the targeted entities possess an active UmweltEngine instance.")
    );

    /**
     * Injects the 'emotion' subtree into the shared target argument node.
     */
    public static void build(ArgumentBuilder<CommandSourceStack, ?> parentNode) {
        parentNode.then(Commands.literal("emotion")
                // /umwelt <targets> emotion set <v> <a> <e>
                .then(Commands.literal("set")
                        .then(Commands.argument("valence", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                .then(Commands.argument("arousal", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                        .then(Commands.argument("energy", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                .executes(EmotionSubCommand::executeSetEmotion)))))
                // /umwelt <targets> emotion modify <vDelta> <aDelta> <eDelta>
                .then(Commands.literal("modify")
                        .then(Commands.argument("valence_delta", FloatArgumentType.floatArg(-2.0f, 2.0f))
                                .then(Commands.argument("arousal_delta", FloatArgumentType.floatArg(-2.0f, 2.0f))
                                        .then(Commands.argument("energy_delta", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                .executes(EmotionSubCommand::executeModifyEmotion))))));
    }

    private static int executeSetEmotion(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends net.minecraft.world.entity.Entity> targets = EntityArgument.getEntities(ctx, "targets");
        float v = FloatArgumentType.getFloat(ctx, "valence");
        float a = FloatArgumentType.getFloat(ctx, "arousal");
        float e = FloatArgumentType.getFloat(ctx, "energy");

        int successCount = 0;
        for (var entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                EmotionAPI.setEmotionalState(engineOpt.get(), v, a, e);
                successCount++;
            }
        }

        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Successfully forced emotional circumplex states across " + finalSuccessCount + " entities."
        ), true);

        return successCount;
    }

    private static int executeModifyEmotion(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends net.minecraft.world.entity.Entity> targets = EntityArgument.getEntities(ctx, "targets");
        float vDelta = FloatArgumentType.getFloat(ctx, "valence_delta");
        float aDelta = FloatArgumentType.getFloat(ctx, "arousal_delta");
        float eDelta = FloatArgumentType.getFloat(ctx, "energy_delta");

        int successCount = 0;
        for (var entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                UmweltEngine engine = engineOpt.get();
                float currentV = engine.getEmotionalEngine().getValence();
                float currentA = engine.getEmotionalEngine().getArousal();
                float currentE = engine.getEmotionalEngine().getEnergy();

                EmotionAPI.setEmotionalState(engine, currentV + vDelta, currentA + aDelta, currentE + eDelta);
                successCount++;
            }
        }

        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Modified emotional states relatively for " + finalSuccessCount + " entities."
        ), true);

        return successCount;
    }
}