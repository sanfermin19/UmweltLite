package com.fermine.umweltlite.api.command;

import com.fermine.umweltlite.api.engine.MemoryAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.goals.engine.memory.memory.Memory;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class MemorySubCommand {

    private static final SimpleCommandExceptionType ERROR_NOT_UMWELT_COMPATIBLE = new SimpleCommandExceptionType(
            Component.literal("None of the targeted entities possess an active UmweltEngine instance.")
    );

    public static void build(ArgumentBuilder<CommandSourceStack, ?> parentNode) {
        parentNode.then(Commands.literal("memory")
                .then(Commands.literal("inject")
                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                .then(Commands.argument("valence", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                        .then(Commands.argument("arousal", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                .then(Commands.argument("energy", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                        .executes(MemorySubCommand::executeInjectMemory))))))
                .then(Commands.literal("reevaluate")
                        .then(Commands.argument("contextKey", StringArgumentType.word())
                                .then(Commands.argument("contextValue", StringArgumentType.word())
                                        .then(Commands.argument("newValence", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                .then(Commands.argument("newArousal", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                        .executes(MemorySubCommand::executeReevaluateMemory))))))
                .then(Commands.literal("wipe")
                        .then(Commands.argument("contextKey", StringArgumentType.word())
                                .then(Commands.argument("contextValue", StringArgumentType.word())
                                        .executes(MemorySubCommand::executeWipeSpecificMemories))))
                .then(Commands.literal("clear_expired")
                        .then(Commands.argument("lifespan", LongArgumentType.longArg(1L))
                                .executes(MemorySubCommand::executeWipeExpiredMemories)))
                .then(Commands.literal("query_recent")
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 50))
                                .executes(MemorySubCommand::executeGetRecentMemories)))
                .then(Commands.literal("check_context")
                        .then(Commands.argument("contextKey", StringArgumentType.word())
                                .then(Commands.argument("contextValue", StringArgumentType.word())
                                        .executes(MemorySubCommand::executeHasMemoryWithContext))))
                .then(Commands.literal("count_significant")
                        .executes(MemorySubCommand::executeCountSignificantMemories)));
    }

    private static int executeInjectMemory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
        float v = FloatArgumentType.getFloat(ctx, "valence");
        float a = FloatArgumentType.getFloat(ctx, "arousal");
        float e = FloatArgumentType.getFloat(ctx, "energy");

        EmotionalMap impact = new EmotionalMap(v, a, e);
        CompoundTag meta = new CompoundTag();
        meta.putString("type", "synthetic_command_injection");

        int successCount = 0;
        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                MemoryAPI.injectMemory(engineOpt.get(), pos, impact, meta);
                successCount++;
            }
        }
        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal("Injected memory into " + finalSuccessCount + " nodes."), true);
        return successCount;
    }

    private static int executeReevaluateMemory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String key = StringArgumentType.getString(ctx, "contextKey");
        String value = StringArgumentType.getString(ctx, "contextValue");
        float nv = FloatArgumentType.getFloat(ctx, "newValence");
        float na = FloatArgumentType.getFloat(ctx, "newArousal");

        int successCount = 0;
        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                MemoryAPI.reevaluateMemory(engineOpt.get(), key, value, nv, na);
                successCount++;
            }
        }
        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal("Reevaluated memories for " + finalSuccessCount + " nodes."), true);
        return successCount;
    }

    private static int executeWipeSpecificMemories(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String key = StringArgumentType.getString(ctx, "contextKey");
        String value = StringArgumentType.getString(ctx, "contextValue");

        int successCount = 0;
        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                MemoryAPI.wipeSpecificMemories(engineOpt.get(), key, value);
                successCount++;
            }
        }
        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        int finalSuccessCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal("Wiped memories for " + finalSuccessCount + " nodes."), true);
        return successCount;
    }

    private static int executeWipeExpiredMemories(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        long customLifespan = LongArgumentType.getLong(ctx, "lifespan");

        int successCount = 0;
        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                UmweltEngine engine = engineOpt.get();
                MemoryAPI.wipeMemoriesIf(engine, memory -> MemoryAPI.isExpired(engine, memory, customLifespan));
                successCount++;
            }
        }
        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();

        ctx.getSource().sendSuccess(() -> Component.literal("Cleared expired memories."), true);
        return successCount;
    }

    private static int executeGetRecentMemories(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        int count = IntegerArgumentType.getInteger(ctx, "count");

        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                List<Memory> recent = MemoryAPI.getRecent(engineOpt.get(), count);
                ctx.getSource().sendSuccess(() -> Component.literal("Recent memories for " + entity.getScoreboardName() + ": " + recent.size()), false);
            }
        }
        return targets.size();
    }

    private static int executeHasMemoryWithContext(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String key = StringArgumentType.getString(ctx, "contextKey");
        String value = StringArgumentType.getString(ctx, "contextValue");

        int foundCount = 0;
        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent() && MemoryAPI.hasMemoryWithContext(engineOpt.get(), key, value)) {
                foundCount++;
            }
        }
        return foundCount;
    }

    private static int executeCountSignificantMemories(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        int totalSignificant = 0;
        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                totalSignificant += (int) MemoryAPI.countMemoriesMatching(engineOpt.get(), MemoryAPI::isSignificant);
            }
        }
        return totalSignificant;
    }
}