package com.fermine.umweltlite.api.command;

import com.fermine.umweltlite.api.engine.PersonalityAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class PersonalitySubCommand {

    public static void build(ArgumentBuilder<CommandSourceStack, ?> parentNode) {
        parentNode.then(Commands.literal("personality")
                // --- Setters / Modifiers ---
                .then(Commands.literal("apply")
                        .then(Commands.argument("templateId", StringArgumentType.word())
                                .executes(PersonalitySubCommand::executeApplyTemplate)))
                .then(Commands.literal("override")
                        .then(Commands.argument("trait", StringArgumentType.word())
                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                                        .executes(PersonalitySubCommand::executeOverrideTrait))))
                .then(Commands.literal("clear_override")
                        .then(Commands.argument("trait", StringArgumentType.word())
                                .executes(PersonalitySubCommand::executeClearOverride)))

                // --- Getters / Inspectors (NEW) ---
                .then(Commands.literal("get_trait")
                        .then(Commands.argument("trait", StringArgumentType.word())
                                .executes(PersonalitySubCommand::executeGetTrait)))
                .then(Commands.literal("check_social")
                        .executes(PersonalitySubCommand::executeCheckSocial)));
    }

    // --- Modifier Execution Methods ---

    private static int executeApplyTemplate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String templateId = StringArgumentType.getString(ctx, "templateId");

        int count = 0;
        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> PersonalityAPI.applyTemplate(e, templateId));
            count++;
        }
        int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal("Applied template " + templateId + " to " + finalCount + " entities."), true);
        return count;
    }

    private static int executeOverrideTrait(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String trait = StringArgumentType.getString(ctx, "trait");
        float value = FloatArgumentType.getFloat(ctx, "value");

        int count = 0;
        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> PersonalityAPI.overrideTrait(e, trait, value));
            count++;
        }
        int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal("Set " + trait + " to " + value + " for " + finalCount + " entities."), true);
        return count;
    }

    private static int executeClearOverride(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String trait = StringArgumentType.getString(ctx, "trait");

        int count = 0;
        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> PersonalityAPI.clearOverride(e, trait));
            count++;
        }
        int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal("Cleared override for " + trait + " on " + finalCount + " entities."), true);
        return count;
    }

    // --- Inspection Execution Methods ---

    private static int executeGetTrait(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String trait = StringArgumentType.getString(ctx, "trait");

        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresentOrElse(engine -> {
                float value = PersonalityAPI.getTrait(engine, trait);
                ctx.getSource().sendSuccess(() -> Component.literal(
                        entity.getName().getString() + " [" + trait + "]: " + String.format("%.3f", value)), false);
            }, () -> ctx.getSource().sendFailure(Component.literal(entity.getName().getString() + " lacks an UmweltEngine.")));
        }
        return targets.size();
    }

    private static int executeCheckSocial(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");

        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresentOrElse(engine -> {
                boolean isSocial = PersonalityAPI.isSocial(engine);
                ctx.getSource().sendSuccess(() -> Component.literal(
                        entity.getName().getString() + " is social: " + (isSocial ? "§aTrue" : "§cFalse")), false);
            }, () -> ctx.getSource().sendFailure(Component.literal(entity.getName().getString() + " lacks an UmweltEngine.")));
        }
        return targets.size();
    }
}