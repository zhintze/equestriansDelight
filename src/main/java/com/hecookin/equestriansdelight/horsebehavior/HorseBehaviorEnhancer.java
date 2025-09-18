package com.hecookin.equestriansdelight.horsebehavior;

import com.hecookin.equestriansdelight.Config;
import com.hecookin.equestriansdelight.EquestriansDelight;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = EquestriansDelight.MODID)
public class HorseBehaviorEnhancer {

    // Track buoyancy state for each horse
    private static final Map<UUID, Integer> horseBuoyancyCooldown = new HashMap<>();
    private static final Map<UUID, Double> horsePeakHeight = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof AbstractHorse horse) {
            // Swimming when ridden
            if (Config.HORSE_SWIMMING_WHEN_RIDDEN.get() && horse.isVehicle()) {
                if (horse.isInWater()) {
                    UUID horseId = horse.getUUID();

                    // Force swimming behavior
                    horse.setSwimming(true);

                    // Calculate horizontal speed
                    double horizontalSpeed = Math.sqrt(
                        horse.getDeltaMovement().x * horse.getDeltaMovement().x +
                        horse.getDeltaMovement().z * horse.getDeltaMovement().z
                    );

                    // Track peak height for cooldown system
                    double currentHeight = horse.getY();
                    Double peakHeight = horsePeakHeight.get(horseId);
                    if (peakHeight == null || currentHeight > peakHeight) {
                        horsePeakHeight.put(horseId, currentHeight);
                    }

                    // Get current cooldown
                    Integer cooldown = horseBuoyancyCooldown.getOrDefault(horseId, 0);
                    if (cooldown > 0) {
                        horseBuoyancyCooldown.put(horseId, cooldown - 1);
                    }

                    if (horizontalSpeed > 0.01) {
                        // Moving - check if we should apply buoyancy
                        boolean shouldApplyBuoyancy = true;

                        // If we're near peak height and have been going up, start cooldown
                        if (peakHeight != null && currentHeight >= peakHeight - 0.1 && horse.getDeltaMovement().y > 0.1) {
                            horseBuoyancyCooldown.put(horseId, 20); // 1 second pause at 20 ticks
                            shouldApplyBuoyancy = false;
                        }

                        if (shouldApplyBuoyancy && cooldown <= 0) {
                            // Apply speed-based buoyancy
                            double speedBasedBuoyancy = Math.min(horizontalSpeed * 0.3, 0.15); // Cap at 0.15

                            if (horse.getDeltaMovement().y < 0.05) {
                                horse.setDeltaMovement(
                                    horse.getDeltaMovement().x,
                                    horse.getDeltaMovement().y + speedBasedBuoyancy,
                                    horse.getDeltaMovement().z
                                );
                            }
                        }

                        // Prevent excessive sinking while moving
                        if (horse.getDeltaMovement().y < -0.02) {
                            horse.setDeltaMovement(
                                horse.getDeltaMovement().x,
                                Math.max(horse.getDeltaMovement().y, -0.02),
                                horse.getDeltaMovement().z
                            );
                        }
                    } else {
                        // Not moving - reset peak height and allow controlled sinking
                        horsePeakHeight.remove(horseId);
                        horseBuoyancyCooldown.remove(horseId);

                        if (horse.getDeltaMovement().y < -0.1) {
                            horse.setDeltaMovement(
                                horse.getDeltaMovement().x,
                                horse.getDeltaMovement().y * 0.9 + 0.01, // Slight buoyancy when stationary
                                horse.getDeltaMovement().z
                            );
                        }
                    }
                } else {
                    // Not in water - clean up tracking data
                    UUID horseId = horse.getUUID();
                    horsePeakHeight.remove(horseId);
                    horseBuoyancyCooldown.remove(horseId);
                }
            }
        }
    }
}