package com.fermine.umweltlite.api.command;

import com.fermine.umweltlite.api.engine.EmotionAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.emotion.map.AttachmentMap;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class SocialSubCommand {

    private static final SimpleCommandExceptionType ERROR_NOT_UMWELT_COMPATIBLE = new SimpleCommandExceptionType(
            Component.literal("None of the targeted entities possess an active UmweltEngine instance.")
    );

    /**
     * Injects the 'social' subtree into the shared target argument node.
     */
    public static void build(ArgumentBuilder<CommandSourceStack, ?> parentNode) {
        parentNode.then(Commands.literal("social")
                // /umwelt <targets> social set <UUID> <v> <a> <b>
                .then(Commands.literal("set")
                        .then(Commands.argument("relationTarget", UuidArgument.uuid())
                                .then(Commands.argument("valence", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                        .then(Commands.argument("arousal", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                .then(Commands.argument("bond", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                        .executes(SocialSubCommand::executeSetSocialAttachment))))))

                // /umwelt <targets> social modify <UUID> <vDelta> <iDelta> <bDelta>
                .then(Commands.literal("modify")
                        .then(Commands.argument("relationTarget", UuidArgument.uuid())
                                .then(Commands.argument("vDelta", FloatArgumentType.floatArg(-2.0f, 2.0f))
                                        .then(Commands.argument("iDelta", FloatArgumentType.floatArg(-2.0f, 2.0f))
                                                .then(Commands.argument("bDelta", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                        .executes(SocialSubCommand::executeModifySocialAttachment))))))

                // /umwelt <targets> social clear <UUID>
                .then(Commands.literal("clear")
                        .then(Commands.argument("relationTarget", UuidArgument.uuid())
                                .executes(SocialSubCommand::executeClearSocialAttachment)))

                // /umwelt <targets> social check_mate <UUID>
                .then(Commands.literal("check_mate")
                        .then(Commands.argument("relationTarget", UuidArgument.uuid())
                                .executes(SocialSubCommand::executeCheckMate))));
    }

    private static int executeSetSocialAttachment(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends net.minecraft.world.entity.Entity> targets = EntityArgument.getEntities(ctx, "targets");
        UUID relationTarget = UuidArgument.getUuid(ctx, "relationTarget");
        float v = FloatArgumentType.getFloat(ctx, "valence");
        float a = FloatArgumentType.getFloat(ctx, "arousal");
        float b = FloatArgumentType.getFloat(ctx, "bond");

        int successCount = 0;
        AttachmentMap map = new AttachmentMap(v, a, b);

        for (var entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                EmotionAPI.setSocialAttachment(engineOpt.get(), relationTarget, map);
                successCount++;
            }
        }

        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Injected social attachment mapping for target UUID [" + relationTarget + "] across " + finalSuccessCount + " entities."
        ), true);

        return successCount;
    }

    private static int executeModifySocialAttachment(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends net.minecraft.world.entity.Entity> targets = EntityArgument.getEntities(ctx, "targets");
        UUID relationTarget = UuidArgument.getUuid(ctx, "relationTarget");
        float vDelta = FloatArgumentType.getFloat(ctx, "vDelta");
        float iDelta = FloatArgumentType.getFloat(ctx, "iDelta");
        float bDelta = FloatArgumentType.getFloat(ctx, "bDelta");

        int successCount = 0;
        for (var entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                EmotionAPI.modifySocialAttachment(engineOpt.get(), relationTarget, vDelta, iDelta, bDelta);
                successCount++;
            }
        }

        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Shifted social relationship markers relatively for target UUID [" + relationTarget + "] across " + finalSuccessCount + " entities."
        ), true);

        return successCount;
    }

    private static int executeClearSocialAttachment(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends net.minecraft.world.entity.Entity> targets = EntityArgument.getEntities(ctx, "targets");
        UUID relationTarget = UuidArgument.getUuid(ctx, "relationTarget");

        int successCount = 0;
        for (var entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                EmotionAPI.clearSocialAttachment(engineOpt.get(), relationTarget);
                successCount++;
            }
        }

        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Purged relationship memory entry for target UUID [" + relationTarget + "] from " + finalSuccessCount + " entities."
        ), true);

        return successCount;
    }

    private static int executeCheckMate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends net.minecraft.world.entity.Entity> targets = EntityArgument.getEntities(ctx, "targets");
        UUID relationTarget = UuidArgument.getUuid(ctx, "relationTarget");

        int mateValidations = 0;
        int checkedCount = 0;

        for (var entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                checkedCount++;
                if (EmotionAPI.isMate(engineOpt.get(), relationTarget)) {
                    mateValidations++;
                }
            }
        }

        if (checkedCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalValidations = mateValidations;
        int finalChecked = checkedCount;
        ctx.getSource().sendSuccess(() -> Component.literal(
                "Mating evaluation: " + finalValidations + " of " + finalChecked + " checked entities validate target [" + relationTarget + "] as mate."
        ), false);

        return mateValidations;
    }
}