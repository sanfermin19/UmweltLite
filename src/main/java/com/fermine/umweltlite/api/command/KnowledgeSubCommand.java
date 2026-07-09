package com.fermine.umweltlite.api.command;

import com.fermine.umweltlite.api.engine.KnowledgeAPI;
import com.fermine.umweltlite.api.engine.UmweltAPI;
import com.fermine.umweltlite.goals.engine.UmweltEngine;
import com.fermine.umweltlite.goals.engine.knowledge.entry.KnowledgeEntry;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class KnowledgeSubCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final SimpleCommandExceptionType ERROR_NOT_UMWELT_COMPATIBLE = new SimpleCommandExceptionType(
            Component.literal("None of the targeted entities possess an active UmweltEngine instance.")
    );

    private static final SimpleCommandExceptionType ERROR_NO_SPATIAL_TARGET = new SimpleCommandExceptionType(
            Component.literal("The evaluated entity does not possess any valid historical target locations.")
    );

    public static void build(ArgumentBuilder<CommandSourceStack, ?> parentNode, CommandBuildContext context) {
        parentNode.then(Commands.literal("knowledge")
                // We add the targets argument HERE so it's available to all children
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.literal("bias")
                                .then(Commands.argument("entityType", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
                                        .then(Commands.argument("valence", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                .then(Commands.argument("arousal", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                        .executes(KnowledgeSubCommand::executeSetEmotionalBias)))))
                        .then(Commands.literal("fact_set")
                                .then(Commands.argument("factKey", StringArgumentType.word())
                                        .executes(KnowledgeSubCommand::executeSetPermanentFact)))
                        .then(Commands.literal("spatial_set")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("confidence", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                .executes(KnowledgeSubCommand::executeSetSpatialMemory))))
                        .then(Commands.literal("find_target")
                                .executes(KnowledgeSubCommand::executeFindUmweltTarget))
                        .then(Commands.literal("clear")
                                .executes(KnowledgeSubCommand::executeClearAllKnowledge))
                        .then(Commands.literal("fact_wipe")
                                .then(Commands.argument("factKey", StringArgumentType.word())
                                        .executes(KnowledgeSubCommand::executeWipeFact)))
                        .then(Commands.literal("spatial_conf")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("halfLife", LongArgumentType.longArg(1L))
                                                .executes(KnowledgeSubCommand::executeGetSpatialConfidence))))
                        .then(Commands.literal("fact_stale")
                                .then(Commands.argument("factKey", StringArgumentType.word())
                                        .then(Commands.argument("lifespan", LongArgumentType.longArg(1L))
                                                .executes(KnowledgeSubCommand::executeCheckFactStale))))
                        .then(Commands.literal("spatial_stale")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .then(Commands.argument("lifespan", LongArgumentType.longArg(1L))
                                                .executes(KnowledgeSubCommand::executeCheckSpatialStale))))
                        .then(Commands.literal("dump")
                                .executes(KnowledgeSubCommand::executeDumpKnowledge))
                )
        );
    }

    // --- EXECUTION METHODS ---

    private static int executeDumpKnowledge(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");

        for (Entity e : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(e);
            if (engineOpt.isPresent()) {
                UmweltEngine engine = engineOpt.get();
                LOGGER.info("=== UmweltEngine Knowledge Dump: {} ===", e.getName().getString());

                LOGGER.info("--- Fact Map ---");
                Map<String, KnowledgeEntry> facts = KnowledgeAPI.getFactMap(engine);
                if (facts.isEmpty()) LOGGER.info("No facts recorded.");
                facts.forEach((key, entry) -> LOGGER.info("Fact: '{}' | Data: {}", key, entry.toString()));

                LOGGER.info("--- Spatial Map ---");
                Map<BlockPos, KnowledgeEntry> spatial = KnowledgeAPI.getSpatialMap(engine);
                if (spatial.isEmpty()) LOGGER.info("No spatial data recorded.");
                spatial.forEach((pos, entry) -> LOGGER.info("Pos: {} | Data: {}", pos, entry.toString()));
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal("Knowledge data dumped to server console."), false);
        return targets.size();
    }

    private static int executeWipeFact(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String key = StringArgumentType.getString(ctx, "factKey");
        int count = 0;
        for (Entity e : targets) {
            UmweltAPI.getEngine(e).ifPresent(engine -> KnowledgeAPI.wipeFact(engine, key));
            count++;
        }
        int finalCount = count;
        ctx.getSource().sendSuccess(() -> Component.literal("Fact wiped for " + finalCount + " nodes."), true);
        return count;
    }

    private static int executeGetSpatialConfidence(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        long halfLife = LongArgumentType.getLong(ctx, "halfLife");

        for (Entity e : targets) {
            UmweltAPI.getEngine(e).ifPresent(engine -> {
                float conf = KnowledgeAPI.getSpatialConfidence(engine, pos, halfLife);
                ctx.getSource().sendSuccess(() -> Component.literal(e.getName().getString() + " confidence at " + pos + ": " + String.format("%.2f", conf)), false);
            });
        }
        return targets.size();
    }

    private static int executeCheckFactStale(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String key = StringArgumentType.getString(ctx, "factKey");
        long lifespan = LongArgumentType.getLong(ctx, "lifespan");

        for (Entity e : targets) {
            UmweltAPI.getEngine(e).ifPresent(engine -> {
                boolean stale = KnowledgeAPI.isFactStale(engine, key, lifespan);
                ctx.getSource().sendSuccess(() -> Component.literal(e.getName().getString() + " fact '" + key + "' is stale: " + stale), false);
            });
        }
        return targets.size();
    }

    private static int executeCheckSpatialStale(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        long lifespan = LongArgumentType.getLong(ctx, "lifespan");

        for (Entity e : targets) {
            UmweltAPI.getEngine(e).ifPresent(engine -> {
                boolean stale = KnowledgeAPI.isSpatialStale(engine, pos, lifespan);
                ctx.getSource().sendSuccess(() -> Component.literal(e.getName().getString() + " spatial node at " + pos + " is stale: " + stale), false);
            });
        }
        return targets.size();
    }

    private static int executeSetEmotionalBias(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        EntityType<?> type = ResourceArgument.getEntityType(ctx, "entityType").value();
        float v = FloatArgumentType.getFloat(ctx, "valence");
        float a = FloatArgumentType.getFloat(ctx, "arousal");
        int successCount = 0;
        for (Entity entity : targets) {
            if(UmweltAPI.getEngine(entity).isPresent()) {
                KnowledgeAPI.setEmotionalBias(UmweltAPI.getEngine(entity).get(), type, v, a);
                successCount++;
            }
        }
        if (successCount == 0) throw ERROR_NOT_UMWELT_COMPATIBLE.create();
        int finalCount = successCount;
        ctx.getSource().sendSuccess(() -> Component.literal("Bias profile updated for " + finalCount + " target nodes."), true);
        return successCount;
    }

    private static int executeSetPermanentFact(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        String key = StringArgumentType.getString(ctx, "factKey");
        CompoundTag meta = new CompoundTag();
        meta.putLong("timestamp", ctx.getSource().getLevel().getGameTime());
        int successCount = 0;
        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> KnowledgeAPI.setPermanentFact(e, key, meta));
            successCount++;
        }
        return successCount;
    }

    private static int executeSetSpatialMemory(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        float conf = FloatArgumentType.getFloat(ctx, "confidence");
        int successCount = 0;
        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(e -> KnowledgeAPI.setSpatialMemory(e, pos, new CompoundTag(), conf));
            successCount++;
        }
        return successCount;
    }

    private static int executeFindUmweltTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        int found = 0;
        for (Entity entity : targets) {
            Optional<UmweltEngine> engineOpt = UmweltAPI.getEngine(entity);
            if (engineOpt.isPresent()) {
                Vec3 target = KnowledgeAPI.findUmweltTarget(engineOpt.get());
                if (target != null) {
                    found++;
                    ctx.getSource().sendSuccess(() -> Component.literal("Target resolved: " + target), false);
                }
            }
        }
        if (found == 0) throw ERROR_NO_SPATIAL_TARGET.create();
        return found;
    }

    private static int executeClearAllKnowledge(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> targets = EntityArgument.getEntities(ctx, "targets");
        int count = 0;
        for (Entity entity : targets) {
            UmweltAPI.getEngine(entity).ifPresent(KnowledgeAPI::clearAllKnowledge);
            count++;
        }
        return count;
    }
}