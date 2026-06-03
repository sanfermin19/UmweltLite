package com.fermine.umweltlite.api.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public class UmweltCommand {

    /**
     * Registers the root /umwelt command node and delegates subtrees to their
     * respective specialized sub-command architect classes.
     *
     * @param dispatcher The Brigadier CommandDispatcher
     * @param context    The CommandBuildContext required for registry-based arguments
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        // Define the root command
        var rootNode = Commands.literal("umwelt")
                .requires(source -> source.hasPermission(2)); // Requires OP level 2

        // Create the common target argument node
        var targetsArgument = Commands.argument("targets", EntityArgument.entities());

        // Build and inject the subtrees
        // We pass the context to those that need it for registry lookups
        EmotionSubCommand.build(targetsArgument);
        SocialSubCommand.build(targetsArgument);
        KnowledgeSubCommand.build(targetsArgument, context);
        MemorySubCommand.build(targetsArgument);
        PersonalitySubCommand.build(targetsArgument);
        SensorySubCommand.build(targetsArgument);

        // Attach the target branch to the root and register
        rootNode.then(targetsArgument);
        dispatcher.register(rootNode);
    }
}