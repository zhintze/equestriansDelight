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
    private static final Map<UUID, Boolean> horseAboveWater = new HashMap<>();

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

                    // Check if horse head is above water surface (simple approach)
                    boolean isAboveWater = !horse.isUnderWater();

                    // Track if horse transitions from underwater to above water
                    Boolean wasAboveWater = horseAboveWater.get(horseId);
                    if (wasAboveWater == null) wasAboveWater = false;

                    // Get current cooldown
                    Integer cooldown = horseBuoyancyCooldown.getOrDefault(horseId, 0);
                    if (cooldown > 0) {
                        horseBuoyancyCooldown.put(horseId, cooldown - 1);
                    }

                    if (horizontalSpeed > 0.01) {
                        // Moving - check if we should apply buoyancy
                        boolean shouldApplyBuoyancy = true;

                        // If horse goes above water surface, start cooldown
                        if (!wasAboveWater && isAboveWater && cooldown <= 0) {
                            horseBuoyancyCooldown.put(horseId, 100); // 5 second pause
                            shouldApplyBuoyancy = false;
                        }

                        // Update the above water tracking
                        horseAboveWater.put(horseId, isAboveWater);

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

                        // Prevent excessive sinking while moving (only when not in cooldown)
                        if (cooldown <= 0 && horse.getDeltaMovement().y < -0.02) {
                            horse.setDeltaMovement(
                                horse.getDeltaMovement().x,
                                Math.max(horse.getDeltaMovement().y, -0.02),
                                horse.getDeltaMovement().z
                            );
                        }
                    } else {
                        // Not moving - update above water tracking but keep cooldown
                        horseAboveWater.put(horseId, isAboveWater);
                        // Don't remove cooldown when not moving - let it continue counting down

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
                    horseAboveWater.remove(horseId);
                    horseBuoyancyCooldown.remove(horseId);
                }
            }
        }
    }
}