package com.hecookin.equestriansdelight;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    // Horse Breeding Configuration
    public static final ModConfigSpec.BooleanValue ENABLE_IMPROVED_BREEDING = BUILDER
            .comment("Enable improved horse breeding system")
            .define("enableImprovedBreeding", true);

    public static final ModConfigSpec.BooleanValue REMOVE_SPEED_LIMITS = BUILDER
            .comment("Remove vanilla speed limits for horses")
            .define("removeSpeedLimits", true);

    public static final ModConfigSpec.BooleanValue REMOVE_JUMP_LIMITS = BUILDER
            .comment("Remove vanilla jump height limits for horses")
            .define("removeJumpLimits", true);

    public static final ModConfigSpec.DoubleValue FASTER_PARENT_WEIGHT = BUILDER
            .comment("Weight percentage for faster parent (0.0 to 1.0)")
            .defineInRange("fasterParentWeight", 0.6, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue MIN_STAT_VARIATION = BUILDER
            .comment("Minimum stat variation percentage (-1.0 to 1.0)")
            .defineInRange("minStatVariation", -0.04, -1.0, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_STAT_VARIATION = BUILDER
            .comment("Maximum stat variation percentage (-1.0 to 1.0)")
            .defineInRange("maxStatVariation", 0.08, -1.0, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_SPEED_MULTIPLIER = BUILDER
            .comment("Maximum speed multiplier when limits removed")
            .defineInRange("maxSpeedMultiplier", 3.0, 1.0, 10.0);

    public static final ModConfigSpec.DoubleValue MAX_JUMP_MULTIPLIER = BUILDER
            .comment("Maximum jump height multiplier when limits removed")
            .defineInRange("maxJumpMultiplier", 2.0, 1.0, 5.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}