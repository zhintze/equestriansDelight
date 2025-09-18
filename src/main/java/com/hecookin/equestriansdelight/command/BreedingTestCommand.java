package com.hecookin.equestriansdelight.command;

import com.hecookin.equestriansdelight.Config;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;

public class BreedingTestCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("equestriansdelight")
            .then(Commands.literal("breeding")
                .then(Commands.literal("test")
                    .requires(source -> source.hasPermission(2))
                    .executes(BreedingTestCommand::createTestHorses))
                .then(Commands.literal("spawn")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.1, 2.0))
                        .then(Commands.argument("jump", DoubleArgumentType.doubleArg(0.4, 2.0))
                            .executes(BreedingTestCommand::spawnCustomHorse))))
                .then(Commands.literal("config")
                    .requires(source -> source.hasPermission(2))
                    .executes(BreedingTestCommand::showConfig))
                .then(Commands.literal("stats")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("horse", EntityArgument.entity())
                        .executes(BreedingTestCommand::showHorseStats)))
                .then(Commands.literal("debug")
                    .requires(source -> source.hasPermission(2))
                    .executes(BreedingTestCommand::debugNearbyHorses))));
    }

    private static int createTestHorses(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BlockPos pos = BlockPos.containing(context.getSource().getPosition());

            // Create two test horses with known stats
            Horse horse1 = createTestHorse(level, pos.offset(2, 0, 0), 0.25, 0.8, "FastHorse");
            Horse horse2 = createTestHorse(level, pos.offset(-2, 0, 0), 0.15, 0.6, "SlowHorse");

            context.getSource().sendSuccess(() -> Component.literal(
                "Created test horses:\n" +
                "FastHorse: Speed 0.25 (10.5 b/s), Jump 0.8 (4.3 blocks)\n" +
                "SlowHorse: Speed 0.15 (6.3 b/s), Jump 0.6 (2.8 blocks)\n" +
                "Breed them to test the system!"
            ), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to create test horses: " + e.getMessage()));
            return 0;
        }
    }

    private static int spawnCustomHorse(CommandContext<CommandSourceStack> context) {
        try {
            double speed = DoubleArgumentType.getDouble(context, "speed");
            double jump = DoubleArgumentType.getDouble(context, "jump");

            ServerLevel level = context.getSource().getLevel();
            BlockPos pos = BlockPos.containing(context.getSource().getPosition());

            Horse horse = createTestHorse(level, pos.offset(1, 0, 0), speed, jump, "CustomHorse");

            context.getSource().sendSuccess(() -> Component.literal(
                String.format("Spawned horse with Speed: %.3f (%.1f b/s), Jump: %.3f (%.1f blocks)",
                    speed, speed * 42.16, jump, calculateJumpHeight(jump))
            ), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to spawn custom horse: " + e.getMessage()));
            return 0;
        }
    }

    private static int showConfig(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(
            "=== Breeding Configuration ===\n" +
            "Enabled: " + Config.ENABLE_IMPROVED_BREEDING.get() + "\n" +
            "Remove Speed Limits: " + Config.REMOVE_SPEED_LIMITS.get() + "\n" +
            "Remove Jump Limits: " + Config.REMOVE_JUMP_LIMITS.get() + "\n" +
            "Faster Parent Weight: " + Config.FASTER_PARENT_WEIGHT.get() + "\n" +
            "Stat Variation: " + Config.MIN_STAT_VARIATION.get() + " to " + Config.MAX_STAT_VARIATION.get() + "\n" +
            "Max Speed Multiplier: " + Config.MAX_SPEED_MULTIPLIER.get() + "\n" +
            "Max Jump Multiplier: " + Config.MAX_JUMP_MULTIPLIER.get()
        ), false);
        return 1;
    }

    private static int showHorseStats(CommandContext<CommandSourceStack> context) {
        try {
            var entity = EntityArgument.getEntity(context, "horse");
            if (!(entity instanceof Horse horse)) {
                context.getSource().sendFailure(Component.literal("Target must be a horse!"));
                return 0;
            }

            double speed = horse.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
            double jump = horse.getAttribute(Attributes.JUMP_STRENGTH).getValue();
            double health = horse.getAttribute(Attributes.MAX_HEALTH).getValue();

            context.getSource().sendSuccess(() -> Component.literal(
                String.format("=== Horse Stats ===\n" +
                "Speed: %.4f (%.2f b/s)\n" +
                "Jump: %.4f (%.2f blocks)\n" +
                "Health: %.1f HP\n" +
                "Tamed: %s",
                speed, speed * 42.16,
                jump, calculateJumpHeight(jump),
                health,
                horse.isTamed() ? "Yes" : "No")
            ), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to get horse stats: " + e.getMessage()));
            return 0;
        }
    }

    private static Horse createTestHorse(ServerLevel level, BlockPos pos, double speed, double jump, String name) {
        Horse horse = new Horse(EntityType.HORSE, level);
        horse.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);

        // Set custom stats
        horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        horse.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(jump);
        horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
        horse.setHealth(20.0f);

        // Make it tamed and adult
        horse.setTamed(true);
        horse.setAge(0);
        horse.setCustomName(Component.literal(name));

        level.addFreshEntity(horse);
        return horse;
    }

    private static int debugNearbyHorses(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BlockPos pos = BlockPos.containing(context.getSource().getPosition());

            var horses = level.getEntitiesOfClass(Horse.class,
                net.minecraft.world.phys.AABB.ofSize(pos.getCenter(), 20, 20, 20));

            context.getSource().sendSuccess(() -> Component.literal(
                "=== Nearby Horses Debug ===\n" +
                "Found " + horses.size() + " horses within 10 blocks"
            ), false);

            for (int i = 0; i < horses.size(); i++) {
                Horse horse = horses.get(i);
                String name = horse.hasCustomName() ? horse.getCustomName().getString() : "Horse #" + (i + 1);

                context.getSource().sendSuccess(() -> Component.literal(String.format(
                    "%s:\n" +
                    "  Age: %d (Adult: %s)\n" +
                    "  Tamed: %s\n" +
                    "  In Love: %s\n" +
                    "  Love Ticks: %d\n" +
                    "  Breeding Delay: %d\n" +
                    "  Has Food: %s\n" +
                    "  Position: %.1f, %.1f, %.1f",
                    name,
                    horse.getAge(),
                    horse.getAge() >= 0 ? "Yes" : "No",
                    horse.isTamed() ? "Yes" : "No",
                    horse.isInLove() ? "Yes" : "No",
                    horse.getLoveCause() != null ? 100 : 0, // Approximate love duration
                    horse.getAge() < 0 ? Math.abs(horse.getAge()) : 0,
                    horse.getMainHandItem().isEmpty() ? "No" : "Yes",
                    horse.getX(), horse.getY(), horse.getZ()
                )), false);
            }

            context.getSource().sendSuccess(() -> Component.literal(
                "\n=== Breeding Requirements ===\n" +
                "• Both horses must be adults (age >= 0)\n" +
                "• Both horses must be tamed\n" +
                "• Both horses must be in love (feed golden apples/carrots)\n" +
                "• Horses must be within 8 blocks of each other\n" +
                "• No breeding cooldown active"
            ), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to debug horses: " + e.getMessage()));
            return 0;
        }
    }

    private static double calculateJumpHeight(double jumpStrength) {
        return Math.pow(jumpStrength, 1.7) * 5.293;
    }
}