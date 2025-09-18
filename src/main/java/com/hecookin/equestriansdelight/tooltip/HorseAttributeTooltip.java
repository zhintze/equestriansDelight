package com.hecookin.equestriansdelight.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.DoubleUnaryOperator;

public record HorseAttributeTooltip(@Nullable Item item, @Nullable Holder<MobEffect> icon, Component line1, @Nullable Component line2) implements TooltipComponent {
    private static final DecimalFormat ATTRIBUTE_VALUE_FORMAT = Util.make(new DecimalFormat("#.##"), (p_41704_) -> {
        p_41704_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });

    // Utility method to format numbers with decimals only when non-zero
    public static String formatValue(double value) {
        if (value == Math.floor(value)) {
            // No decimal part, show as integer
            return String.format("%.0f", value);
        } else {
            // Has decimal part, show up to 2 decimal places
            return ATTRIBUTE_VALUE_FORMAT.format(value);
        }
    }

    private HorseAttributeTooltip(Holder<MobEffect> icon, double value, double min, double max, String translationKey) {
        this(null, icon, value, min, max, translationKey, DoubleUnaryOperator.identity());
    }

    private HorseAttributeTooltip(Holder<MobEffect> icon, double value, double min, double max, String translationKey, DoubleUnaryOperator valueConverter) {
        this(null, icon, value, min, max, translationKey, valueConverter);
    }

    private HorseAttributeTooltip(Item item, double value, double min, double max, String translationKey, DoubleUnaryOperator valueConverter) {
        this(item, null, value, min, max, translationKey, valueConverter);
    }

    private HorseAttributeTooltip(@Nullable Item item, @Nullable Holder<MobEffect> icon, double value, double min, double max, String translationKey, DoubleUnaryOperator valueConverter) {
        this(item, icon, line1(value, categorizeValue(value, min, max), translationKey, valueConverter), null);
    }

    private HorseAttributeTooltip(Holder<MobEffect> icon, double value, String translationKey) {
        this(null, icon, value, translationKey, DoubleUnaryOperator.identity());
    }

    private HorseAttributeTooltip(Holder<MobEffect> icon, double value, String translationKey, DoubleUnaryOperator valueConverter) {
        this(null, icon, value, translationKey, valueConverter);
    }

    private HorseAttributeTooltip(Item item, double value, String translationKey, DoubleUnaryOperator valueConverter) {
        this(item, null, value, translationKey, valueConverter);
    }

    private HorseAttributeTooltip(@Nullable Item item, @Nullable Holder<MobEffect> icon, double value, String translationKey, DoubleUnaryOperator valueConverter) {
        this(item, icon, value, translationKey, valueConverter, null);
    }

    private HorseAttributeTooltip(@Nullable Item item, @Nullable Holder<MobEffect> icon, double value, String translationKey, DoubleUnaryOperator valueConverter, @Nullable ChatFormatting valueColor) {
        this(item, icon, line1(value, valueColor, translationKey, valueConverter), null);
    }

    private static Component line1(double value, @Nullable ChatFormatting color, String translationKey, DoubleUnaryOperator valueConverter) {
        MutableComponent component1 = Component.literal(formatValue(valueConverter.applyAsDouble(value)));
        if (color != null) component1 = component1.withStyle(color);
        MutableComponent component2 = Component.translatable(translationKey.concat(".unit"), component1).withStyle(style -> style.withColor(0xAAAAAA));
        return Component.translatable(translationKey, component2).withStyle(ChatFormatting.WHITE);
    }

    private static ChatFormatting categorizeValue(double value, double min, double max) {
        double range = max - min;
        if (value < min + range * 0.25) {
            return ChatFormatting.RED;
        } else if (value >= min + range * 0.75) {
            return ChatFormatting.GREEN;
        }
        return ChatFormatting.GOLD;
    }

    public static HorseAttributeTooltip healthTooltip(double value, boolean minMax) {
        // half health values as our translation string says hearts
        return new HorseAttributeTooltip(null, MobEffects.HEALTH_BOOST, value / 2.0, "horse.tooltip.health", DoubleUnaryOperator.identity(), ChatFormatting.RED);
    }

    public static HorseAttributeTooltip speedTooltip(double value, boolean minMax) {
        // Use same calculation as inventory UI: speedInternal * 42.16
        return new HorseAttributeTooltip(null, MobEffects.MOVEMENT_SPEED, value, "horse.tooltip.speed", d -> d * 42.16, ChatFormatting.GREEN);
    }

    public static HorseAttributeTooltip jumpHeightTooltip(double value, boolean minMax) {
        // Use same calculation as inventory UI: simplified linear approximation
        return new HorseAttributeTooltip(null, MobEffects.JUMP, value, "horse.tooltip.jump_height", HorseAttributeTooltip::calculateJumpHeight, ChatFormatting.AQUA);
    }

    // Use Horse Expert's accurate jump height calculation
    private static double calculateJumpHeight(double jumpStrength) {
        // Power function approximation that matches Minecraft's physics better
        return Math.pow(jumpStrength, 1.7) * 5.293;
    }

    public static HorseAttributeTooltip strengthTooltip(double value) {
        return new HorseAttributeTooltip(Items.CHEST, value, 1.0, 5.0, "horse.tooltip.strength", d -> d * 3);
    }

    public static HorseAttributeTooltip nameTooltip(String displayText) {
        return new HorseAttributeTooltip(null, null, Component.literal(displayText).withStyle(ChatFormatting.YELLOW), null);
    }
}