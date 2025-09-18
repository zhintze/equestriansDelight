package com.hecookin.equestriansdelight.breeding;

import com.hecookin.equestriansdelight.Config;
import com.hecookin.equestriansdelight.EquestriansDelight;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

@EventBusSubscriber(modid = "equestriansdelight")
public class ImprovedHorseBreeding {

    @SubscribeEvent
    public static void onBabyHorseSpawn(BabyEntitySpawnEvent event) {
        // Only process if improved breeding is enabled
        if (!Config.ENABLE_IMPROVED_BREEDING.get()) {
            return;
        }

        // Only apply to horses, donkeys, mules
        if (!(event.getChild() instanceof AbstractHorse babyHorse) ||
            !(event.getParentA() instanceof AbstractHorse parent1) ||
            !(event.getParentB() instanceof AbstractHorse parent2)) {
            return;
        }

        // Apply improved breeding calculations
        improveHorseStats(babyHorse, parent1, parent2);
    }

    private static void improveHorseStats(AbstractHorse baby, AbstractHorse parent1, AbstractHorse parent2) {
        RandomSource random = baby.getRandom();

        // Improve speed attribute
        if (baby.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
            double newSpeed = calculateImprovedAttribute(
                parent1.getAttribute(Attributes.MOVEMENT_SPEED).getValue(),
                parent2.getAttribute(Attributes.MOVEMENT_SPEED).getValue(),
                random,
                Config.REMOVE_SPEED_LIMITS.get() ? Double.MAX_VALUE : 0.3375, // Vanilla max
                Config.MAX_SPEED_MULTIPLIER.get()
            );
            baby.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(newSpeed);
        }

        // Improve jump strength attribute
        if (baby.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
            double newJump = calculateImprovedAttribute(
                parent1.getAttribute(Attributes.JUMP_STRENGTH).getValue(),
                parent2.getAttribute(Attributes.JUMP_STRENGTH).getValue(),
                random,
                Config.REMOVE_JUMP_LIMITS.get() ? Double.MAX_VALUE : 1.0, // Vanilla max
                Config.MAX_JUMP_MULTIPLIER.get()
            );
            baby.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(newJump);
        }

        // Improve health attribute (optional - can be configured separately)
        if (baby.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
            double newHealth = calculateImprovedAttribute(
                parent1.getAttribute(Attributes.MAX_HEALTH).getValue(),
                parent2.getAttribute(Attributes.MAX_HEALTH).getValue(),
                random,
                50.0, // Allow higher health than vanilla max of 30
                2.0   // Max 2x health multiplier
            );
            baby.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newHealth);
            baby.setHealth((float) newHealth); // Set current health to max
        }
    }

    /**
     * Calculate improved attribute using weighted parent system and configurable variation
     */
    private static double calculateImprovedAttribute(double parent1Value, double parent2Value,
                                                   RandomSource random, double maxLimit,
                                                   double maxMultiplier) {
        // Determine faster and slower parent for weighted calculation
        double fasterParentValue = Math.max(parent1Value, parent2Value);
        double slowerParentValue = Math.min(parent1Value, parent2Value);

        // Get weights from config
        double fasterWeight = Config.FASTER_PARENT_WEIGHT.get();
        double slowerWeight = 1.0 - fasterWeight;

        // Calculate weighted base value (no random third horse)
        double baseValue = (fasterParentValue * fasterWeight) + (slowerParentValue * slowerWeight);

        // Apply configurable stat variation (-4% to +8% by default)
        double minVariation = Config.MIN_STAT_VARIATION.get();
        double maxVariation = Config.MAX_STAT_VARIATION.get();
        double variation = minVariation + (random.nextDouble() * (maxVariation - minVariation));

        double finalValue = baseValue * (1.0 + variation);

        // Apply maximum limits if configured
        if (maxLimit != Double.MAX_VALUE) {
            finalValue = Math.min(finalValue, maxLimit);
        }

        // Ensure we don't exceed reasonable multipliers of parent stats
        double maxParentValue = Math.max(parent1Value, parent2Value);
        finalValue = Math.min(finalValue, maxParentValue * maxMultiplier);

        // Ensure minimum reasonable values
        double minValue = Math.min(parent1Value, parent2Value) * 0.5; // At least 50% of worse parent
        finalValue = Math.max(finalValue, minValue);

        return finalValue;
    }

    /**
     * Utility method to get readable breeding stats for debugging/logging
     */
    public static String getBreedingInfo(AbstractHorse baby, AbstractHorse parent1, AbstractHorse parent2) {
        if (!Config.ENABLE_IMPROVED_BREEDING.get()) {
            return "Improved breeding disabled";
        }

        double babySpeed = baby.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
        double parent1Speed = parent1.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
        double parent2Speed = parent2.getAttribute(Attributes.MOVEMENT_SPEED).getValue();

        double babyJump = baby.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH) ?
            baby.getAttribute(Attributes.JUMP_STRENGTH).getValue() : 0.0;
        double parent1Jump = parent1.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH) ?
            parent1.getAttribute(Attributes.JUMP_STRENGTH).getValue() : 0.0;
        double parent2Jump = parent2.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH) ?
            parent2.getAttribute(Attributes.JUMP_STRENGTH).getValue() : 0.0;

        return String.format("Breeding Result:\nSpeed: %.3f (Parents: %.3f, %.3f)\nJump: %.3f (Parents: %.3f, %.3f)",
            babySpeed, parent1Speed, parent2Speed, babyJump, parent1Jump, parent2Jump);
    }
}