package com.fermine.umweltlite.api.command;

import com.fermine.umweltlite.api.engine.SensoryAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.impl.engine.sensory.inter.ISensory;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.Map;

public class SensorySubCommand {

    // MAPPER: Update these with your actual class implementations
    private static final Map<String, Class<? extends ISensory>> SENSE_MAP = Map.of(
            // "sight", SightSense.class,
            // "sound", SoundSense.class
    );

    public static void build(ArgumentBuilder<CommandSourceStack, ?> parentNode) {
        parentNode.then(Commands.literal("sensory")
                .then(Commands.literal("toggle")
                        .then(Commands.argument("senseType", StringArgumentType.word())
                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                        .executes(SensorySubCommand::executeToggleSense))))
                .then(Commands.literal("bias")
                        .then(Commands.argument("bias", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                .executes(SensorySubCommand::executeSteeringBias)))
                .then(Commands.literal("wipe_memories")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(SensorySubCommand::executeWipeMemories)))));
    }

    private static int executeToggleSense(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String senseKey = StringArgumentType.getString(ctx, "senseType");
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");

        Class<? extends ISensory> clazz = SENSE_MAP.get(senseKey);
        if (clazz == null) return 0; // Or throw custom exception "Unknown sense"

        int count = 0;
        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> SensoryAPI.setSenseEnabled(e, clazz, enabled));
            count++;
        }
        return count;
    }

    private static int executeSteeringBias(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        float bias = FloatArgumentType.getFloat(ctx, "bias");

        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> SensoryAPI.applySteeringBias(e, bias));
        }
        return targets.size();
    }

    private static int executeWipeMemories(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String key = StringArgumentType.getString(ctx, "key");
        String value = StringArgumentType.getString(ctx, "value");

        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> SensoryAPI.wipeSpecificMemories(e, key, value));
        }
        return targets.size();
    }
}