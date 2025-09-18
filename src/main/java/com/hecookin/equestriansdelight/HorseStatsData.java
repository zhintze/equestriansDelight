package com.hecookin.equestriansdelight;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.nbt.CompoundTag;

public class HorseStatsData {

    public static class Stats {
        public final double speedInternal;
        public final double speedBlocksPerSec;
        public final double jumpInternal;
        public final double jumpBlocksApprox;
        public final double healthPoints;
        public final double hearts;
        public final String variant;
        public final boolean isTamed;
        public final String ownerName;

        public Stats(double speedInternal, double speedBlocksPerSec, double jumpInternal,
                    double jumpBlocksApprox, double healthPoints, double hearts,
                    String variant, boolean isTamed, String ownerName) {
            this.speedInternal = speedInternal;
            this.speedBlocksPerSec = speedBlocksPerSec;
            this.jumpInternal = jumpInternal;
            this.jumpBlocksApprox = jumpBlocksApprox;
            this.healthPoints = healthPoints;
            this.hearts = hearts;
            this.variant = variant;
            this.isTamed = isTamed;
            this.ownerName = ownerName;
        }
    }

    public static Stats getHorseStats(AbstractHorse horse) {
        // Get speed
        double speedInternal = horse.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
        double speedBlocksPerSec = speedInternal * 42.16;

        // Get health
        double healthPoints = horse.getAttribute(Attributes.MAX_HEALTH).getValue();
        double hearts = healthPoints / 2.0;

        // Get jump strength (horse-specific attribute)
        double jumpInternal = getJumpStrength(horse);
        double jumpBlocksApprox = calculateJumpHeight(jumpInternal);

        // Get variant info
        String variant = getHorseVariant(horse);

        // Get taming info
        boolean isTamed = horse.isTamed();
        String ownerName = horse.getOwnerUUID() != null ? "Owned" : "Wild";

        return new Stats(speedInternal, speedBlocksPerSec, jumpInternal, jumpBlocksApprox,
                        healthPoints, hearts, variant, isTamed, ownerName);
    }

    private static double getJumpStrength(AbstractHorse horse) {
        // Use the actual JUMP_STRENGTH attribute that Jade uses
        if (horse.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
            return horse.getAttribute(Attributes.JUMP_STRENGTH).getValue();
        }

        // Fallback: try NBT approach
        CompoundTag nbt = new CompoundTag();
        horse.addAdditionalSaveData(nbt);
        if (nbt.contains("jumpStrength")) {
            return nbt.getDouble("jumpStrength");
        }

        // Final fallback - return 0 for entities without jump strength (like llamas)
        return 0.0;
    }

    private static double calculateJumpHeight(double jumpStrength) {
        // Use Horse Expert's accurate formula: power function approximation
        // This matches the physics-based calculation better than linear approximation
        return Math.pow(jumpStrength, 1.7) * 5.293;
    }

    private static String getHorseVariant(AbstractHorse horse) {
        // Handle different mount types
        if (horse instanceof Horse) {
            return getRegularHorseVariant(horse);
        } else if (horse instanceof Llama llama) {
            return getLlamaVariant(llama);
        } else if (horse instanceof Donkey) {
            return "Donkey";
        } else if (horse instanceof Mule) {
            return "Mule";
        } else if (horse instanceof Camel) {
            return "Camel";
        } else {
            // Fallback for any other AbstractHorse types
            return horse.getType().getDescription().getString();
        }
    }

    private static String getRegularHorseVariant(AbstractHorse horse) {
        // Get variant data from NBT
        CompoundTag nbt = new CompoundTag();
        horse.addAdditionalSaveData(nbt);

        int variant = nbt.getInt("Variant");

        // Decode variant (color | (markings << 8))
        int color = variant & 0xFF;
        int markings = (variant >> 8) & 0xFF;

        String colorName = getColorName(color);
        String markingName = getMarkingName(markings);

        if (markingName.equals("None")) {
            return colorName;
        } else {
            return colorName + " with " + markingName;
        }
    }

    private static String getLlamaVariant(Llama llama) {
        // Get llama variant from NBT
        CompoundTag nbt = new CompoundTag();
        llama.addAdditionalSaveData(nbt);

        int variant = nbt.getInt("Variant");
        return switch (variant) {
            case 0 -> "Creamy Llama";
            case 1 -> "White Llama";
            case 2 -> "Brown Llama";
            case 3 -> "Gray Llama";
            default -> "Llama";
        };
    }

    private static String getColorName(int color) {
        return switch (color) {
            case 0 -> "White";
            case 1 -> "Buckskin";
            case 2 -> "Flaxen Chestnut";
            case 3 -> "Bay";
            case 4 -> "Black";
            case 5 -> "Dapple Gray";
            case 6 -> "Dark Bay";
            default -> "Unknown Color";
        };
    }

    private static String getMarkingName(int markings) {
        return switch (markings) {
            case 0 -> "None";
            case 1 -> "Stockings and Blaze";
            case 2 -> "Paint";
            case 3 -> "Snowflake Appaloosa";
            case 4 -> "Sooty";
            default -> "Unknown Markings";
        };
    }
}