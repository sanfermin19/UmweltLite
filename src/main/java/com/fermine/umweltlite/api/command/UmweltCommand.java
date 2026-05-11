package com.fermine.umweltlite.api.command;

import com.fermine.umweltlite.api.engine.*;
import com.fermine.umweltlite.api.entity.IUmweltEntity;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.emotion.map.EmotionalMap;
import com.fermine.umweltlite.engine.memory.memory.Memory;
import com.fermine.umweltlite.engine.sensory.inter.ISensory;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class UmweltCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("umwelt")
                .requires(source -> source.hasPermission(2)) // OP level 2
                .then(Commands.literal("emotion")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.literal("get")
                                        .executes(context -> getEmotion(context.getSource(), EntityArgument.getEntity(context, "target"))))
                                .then(Commands.literal("set")
                                        .then(Commands.literal("valence")
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                        .executes(context -> setEmotion(context.getSource(), EntityArgument.getEntity(context, "target"), "v", FloatArgumentType.getFloat(context, "value")))))
                                        .then(Commands.literal("arousal")
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                        .executes(context -> setEmotion(context.getSource(), EntityArgument.getEntity(context, "target"), "a", FloatArgumentType.getFloat(context, "value")))))
                                        .then(Commands.literal("energy")
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                        .executes(context -> setEmotion(context.getSource(), EntityArgument.getEntity(context, "target"), "e", FloatArgumentType.getFloat(context, "value")))))
                                )
                        )
                )
                .then(Commands.literal("knowledge")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.literal("get")
                                        .executes(context -> getKnowledgeSummary(context.getSource(), EntityArgument.getEntity(context, "target"))))
                                .then(Commands.literal("wipe")
                                        .executes(context -> {
                                            Entity entity = EntityArgument.getEntity(context, "target");
                                            if (entity instanceof IUmweltEntity mob) {
                                                KnowledgeAPI.clearAllKnowledge(mob.getUmweltEngine());
                                                context.getSource().sendSuccess(() -> Component.literal("Brain wiped for " + entity.getName().getString()), true);
                                                return 1;
                                            }
                                            return 0;
                                        }))
                                .then(Commands.literal("set_bias")
                                        .then(Commands.argument("entity_type", ResourceLocationArgument.id())
                                                .then(Commands.argument("valence", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                        .then(Commands.argument("arousal", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                                .executes(context -> setBias(
                                                                        context.getSource(),
                                                                        EntityArgument.getEntity(context, "target"),
                                                                        EntityType.byString(ResourceLocationArgument.getId(context, "entity_type").toString()).orElse(EntityType.PIG),
                                                                        FloatArgumentType.getFloat(context, "valence"),
                                                                        FloatArgumentType.getFloat(context, "arousal")
                                                                ))))))
                        )
                ).then(Commands.literal("memory")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.literal("log")
                                        .executes(context -> getMemoryLog(context.getSource(), EntityArgument.getEntity(context, "target"))))
                                .then(Commands.literal("inject")
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .then(Commands.argument("v", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                        .then(Commands.argument("a", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                                .executes(context -> injectSyntheticMemory(
                                                                        context.getSource(),
                                                                        EntityArgument.getEntity(context, "target"),
                                                                        Vec3Argument.getVec3(context, "pos"),
                                                                        FloatArgumentType.getFloat(context, "v"),
                                                                        FloatArgumentType.getFloat(context, "a")
                                                                ))))))
                        )
                ).then(Commands.literal("personality")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.literal("get")
                                        .executes(context -> getPersonalityTraits(context.getSource(), EntityArgument.getEntity(context, "target"))))
                                .then(Commands.literal("apply")
                                        .then(Commands.argument("templateId", StringArgumentType.word())
                                                .executes(context -> applyTemplate(
                                                        context.getSource(),
                                                        EntityArgument.getEntity(context, "target"),
                                                        StringArgumentType.getString(context, "templateId")))))
                                .then(Commands.literal("override")
                                        .then(Commands.argument("trait", StringArgumentType.word())
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                        .executes(context -> setOverride(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                StringArgumentType.getString(context, "trait"),
                                                                FloatArgumentType.getFloat(context, "value")
                                                        )))))
                                .then(Commands.literal("reset")
                                        .then(Commands.argument("trait", StringArgumentType.word())
                                                .executes(context -> clearOverride(
                                                        context.getSource(),
                                                        EntityArgument.getEntity(context, "target"),
                                                        StringArgumentType.getString(context, "trait")
                                                ))))
                        )
                ).then(Commands.literal("sensory")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.literal("list")
                                        .executes(context -> listSenses(context.getSource(), EntityArgument.getEntity(context, "target"))))
                                .then(Commands.literal("bias")
                                        .then(Commands.argument("value", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                .executes(context -> {
                                                    Entity e = EntityArgument.getEntity(context, "target");
                                                    if (e instanceof IUmweltEntity mob) {
                                                        SensoryAPI.applySteeringBias(mob.getUmweltEngine(), FloatArgumentType.getFloat(context, "value"));
                                                        return 1;
                                                    }
                                                    return 0;
                                                })))
                                .then(Commands.literal("toggle")
                                        .then(Commands.argument("senseName", StringArgumentType.word())
                                                .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                        .executes(context -> toggleSense(
                                                                context.getSource(),
                                                                EntityArgument.getEntity(context, "target"),
                                                                StringArgumentType.getString(context, "senseName"),
                                                                BoolArgumentType.getBool(context, "enabled")
                                                        )))))
                        )
                )
        );

    }

    private static int getEmotion(CommandSourceStack source, Entity entity) {
        if (entity instanceof IUmweltEntity mob) {
            UmweltEngine engine = mob.getUmweltEngine();
            var ee = engine.getEmotionalEngine();

            source.sendSuccess(() -> Component.literal(String.format(
                    "§6Internal State for %s:§r\n Valence: %.2f\n Arousal: %.2f\n Energy: %.2f",
                    entity.getDisplayName().getString(), ee.getValence(), ee.getArousal(), ee.getEnergy()
            )), false);
            return 1;
        }
        source.sendFailure(Component.literal("Entity is not an Umwelt Mob."));
        return 0;
    }

    private static int setEmotion(CommandSourceStack source, Entity entity, String type, float value) {
        if (entity instanceof IUmweltEntity mob) {
            UmweltEngine engine = mob.getUmweltEngine();
            switch (type) {
                case "v" -> EmotionAPI.setValence(engine, value);
                case "a" -> EmotionAPI.setArousal(engine, value);
                case "e" -> EmotionAPI.setEnergy(engine, value);
            }
            source.sendSuccess(() -> Component.literal("Successfully updated " + type + " to " + value), true);
            return 1;
        }
        source.sendFailure(Component.literal("Entity is not an Umwelt Mob."));
        return 0;
    }

    private static int getKnowledgeSummary(CommandSourceStack source, Entity entity) {
        if (entity instanceof IUmweltEntity mob) {
            UmweltEngine engine = mob.getUmweltEngine();
            var ke = engine.getKnowledgeEngine();

            int facts = ke.getFactMap().size();
            int spatial = ke.getSpatialMap().size();

            source.sendSuccess(() -> Component.literal(String.format(
                    "§bKnowledge Summary for %s:§r\n Total Facts: %d\n Spatial Memories: %d",
                    entity.getDisplayName().getString(), facts, spatial
            )), false);

            // Print the last 3 facts recorded for quick debugging
            ke.getFactMap().entrySet().stream().limit(3).forEach(entry ->
                    source.sendSuccess(() -> Component.literal("§7 - " + entry.getKey() + " (conf: " + entry.getValue().confidence() + ")"), false)
            );

            return 1;
        }
        return 0;
    }

    private static int setBias(CommandSourceStack source, Entity entity, EntityType<?> type, float v, float a) {
        if (entity instanceof IUmweltEntity mob) {
            KnowledgeAPI.setEmotionalBias(mob.getUmweltEngine(), type, v, a);
            source.sendSuccess(() -> Component.literal("Injected bias for " + EntityType.getKey(type) + " into " + entity.getName().getString()), true);
            return 1;
        }
        return 0;
    }

    private static int getMemoryLog(CommandSourceStack source, Entity entity) {
        if (entity instanceof IUmweltEntity mob) {
            UmweltEngine engine = mob.getUmweltEngine();
            // Now this method actually exists!
            var memories = engine.getMemoryEngine().getRecentMemories(5);

            source.sendSuccess(() -> Component.literal("§dMemory Log for " + entity.getDisplayName().getString() + ":"), false);

            if (memories.isEmpty()) {
                source.sendSuccess(() -> Component.literal(" §8(The void is empty)"), false);
                return 1;
            }

            // Reverse so the newest is at the top of the chat
            for (int i = memories.size() - 1; i >= 0; i--) {
                Memory mem = memories.get(i);
                source.sendSuccess(() -> Component.literal(String.format(
                        " §7- T:%d | L:%.1f, %.1f | V:%.2f A:%.2f",
                        mem.timestamp(), mem.location().x, mem.location().z,
                        mem.emotionalImpact().valence(), mem.emotionalImpact().arousal()
                )), false);
            }
            return 1;
        }
        source.sendFailure(Component.literal("Target is not an Umwelt entity."));
        return 0;
    }

    private static int injectSyntheticMemory(CommandSourceStack source, Entity entity, Vec3 pos, float v, float a) {
        if (entity instanceof IUmweltEntity mob) {
            CompoundTag context = new CompoundTag();
            context.putString("origin", "divine_command");

            MemoryAPI.injectMemory(mob.getUmweltEngine(), pos, new EmotionalMap(v, a, 1.0f), context);
            source.sendSuccess(() -> Component.literal("Successfully implanted memory into " + entity.getName().getString()), true);
            return 1;
        }
        return 0;
    }

    private static int getPersonalityTraits(CommandSourceStack source, Entity entity) {
        if (entity instanceof IUmweltEntity mob) {
            UmweltEngine engine = mob.getUmweltEngine();
            var pe = engine.getPersonality();

            source.sendSuccess(() -> Component.literal("§ePersonality Traits for " + entity.getName().getString() + ":"), false);
            // Assuming your PersonalityEngine has a way to list active values
            pe.getActiveTraits().forEach((trait, val) ->
                    source.sendSuccess(() -> Component.literal(" §7- " + trait + ": §f" + String.format("%.2f", val)), false)
            );
            return 1;
        }
        return 0;
    }

    private static int applyTemplate(CommandSourceStack source, Entity entity, String templateId) {
        if (entity instanceof IUmweltEntity mob) {
            PersonalityAPI.applyTemplate(mob.getUmweltEngine(), templateId);
            source.sendSuccess(() -> Component.literal("Applied template '" + templateId + "' to " + entity.getName().getString()), true);
            return 1;
        }
        return 0;
    }

    private static int setOverride(CommandSourceStack source, Entity entity, String trait, float value) {
        if (entity instanceof IUmweltEntity mob) {
            PersonalityAPI.overrideTrait(mob.getUmweltEngine(), trait, value);
            source.sendSuccess(() -> Component.literal("Overrode " + trait + " to " + value + " for " + entity.getName().getString()), true);
            return 1;
        }
        return 0;
    }

    private static int clearOverride(CommandSourceStack source, Entity entity, String trait) {
        if (entity instanceof IUmweltEntity mob) {
            PersonalityAPI.clearOverride(mob.getUmweltEngine(), trait);
            source.sendSuccess(() -> Component.literal("Cleared override for " + trait + " on " + entity.getName().getString()), true);
            return 1;
        }
        return 0;
    }

    private static int listSenses(CommandSourceStack source, Entity entity) {
        if (entity instanceof IUmweltEntity mob) {
            UmweltEngine engine = mob.getUmweltEngine();
            source.sendSuccess(() -> Component.literal("§3Active Senses for " + entity.getName().getString() + ":"), false);

            engine.getSensoryEngine().getSenses().forEach(s -> {
                String status = s.isEnabled() ? "§a[ON]" : "§c[OFF]";
                source.sendSuccess(() -> Component.literal(" §7- " + s.getClass().getSimpleName() + " " + status), false);
            });
            return 1;
        }
        return 0;
    }

    private static int toggleSense(CommandSourceStack source, Entity entity, String senseName, boolean enabled) {
        if (entity instanceof IUmweltEntity mob) {
            UmweltEngine engine = mob.getUmweltEngine();
            // We find the sense by matching the simple name (e.g., "OpticalSense")
            for (ISensory sense : engine.getSensoryEngine().getSenses()) {
                if (sense.getClass().getSimpleName().equalsIgnoreCase(senseName)) {
                    // This requires you to add a 'setEnabled(boolean)' to your ISensory interface
                    sense.setEnabled(enabled);
                    source.sendSuccess(() -> Component.literal("Set " + senseName + " to " + (enabled ? "enabled" : "disabled")), true);
                    return 1;
                }
            }
        }
        return 0;
    }
}