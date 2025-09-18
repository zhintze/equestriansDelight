package com.hecookin.equestriansdelight;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
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
        // Try to access jump strength from horse's attributes
        // Jump strength is stored in the horse's genetics/attributes
        CompoundTag nbt = new CompoundTag();
        horse.addAdditionalSaveData(nbt);

        // Jump strength is typically stored as "jumpStrength" in NBT
        if (nbt.contains("jumpStrength")) {
            return nbt.getDouble("jumpStrength");
        }

        // Fallback: try to calculate from horse's movement
        // This is an approximation if direct access fails
        return 0.7; // Default average value
    }

    private static double calculateJumpHeight(double jumpStrength) {
        // Approximate conversion from jump strength to block height
        // This is a rough approximation of the complex physics calculation
        if (jumpStrength <= 0.4) return 1.0;
        if (jumpStrength >= 1.0) return 5.9;

        // Linear approximation for display purposes
        return 1.0 + (jumpStrength - 0.4) * (4.9 / 0.6);
    }

    private static String getHorseVariant(AbstractHorse horse) {
        if (!(horse instanceof Horse)) {
            return "Non-Horse";
        }

        Horse regularHorse = (Horse) horse;

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