package com.hecookin.equestriansdelight.horsebehavior;

import com.hecookin.equestriansdelight.Config;
import com.hecookin.equestriansdelight.EquestriansDelight;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = EquestriansDelight.MODID)
public class HorseCallingHandler {

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!Config.ENABLE_HORSE_CALLING.get()) {
            return;
        }

        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack itemInHand = player.getItemInHand(hand);

        // Check if player is holding a goat horn and right-clicking a horse
        if (itemInHand.getItem() == Items.GOAT_HORN &&
            event.getTarget() instanceof AbstractHorse horse) {

            // Check if player owns the horse
            if (horse.isTamed() && horse.getOwnerUUID() != null &&
                horse.getOwnerUUID().equals(player.getUUID())) {

                // Bind horn to horse using custom data component
                CustomData customData = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag hornTag = customData.copyTag();
                hornTag.putUUID("BoundHorseUUID", horse.getUUID());

                String horseName = horse.hasCustomName() ?
                    horse.getCustomName().getString() :
                    "Horse";

                hornTag.putString("BoundHorseName", horseName);
                itemInHand.set(DataComponents.CUSTOM_DATA, CustomData.of(hornTag));

                // Update horn display name
                itemInHand.set(DataComponents.CUSTOM_NAME, Component.literal("Bound Horn: " + horseName));

                player.displayClientMessage(Component.literal("Horn bound to " + horseName + "!"), true);
                System.out.println("Horn bound to horse: " + horseName + " (UUID: " + horse.getUUID() + ")");

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            } else {
                player.displayClientMessage(Component.literal("You can only bind horns to horses you own!"), true);
            }
        }
    }

    @SubscribeEvent
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!Config.ENABLE_HORSE_CALLING.get()) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        InteractionHand hand = event.getHand();
        ItemStack itemInHand = player.getItemInHand(hand);

        // Check if player is using a bound goat horn
        if (itemInHand.getItem() == Items.GOAT_HORN) {
            CustomData customData = itemInHand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag hornTag = customData.copyTag();

            if (hornTag.contains("BoundHorseUUID")) {
                UUID boundHorseUUID = hornTag.getUUID("BoundHorseUUID");
                String horseName = hornTag.getString("BoundHorseName");

                System.out.println("Calling bound horse: " + horseName + " (UUID: " + boundHorseUUID + ")");

                // Try to find the bound horse in the world
                AbstractHorse foundHorse = findHorseByUUID(level, boundHorseUUID);

                if (foundHorse != null) {
                    System.out.println("Found bound horse, calling it");
                    double distance = foundHorse.distanceTo(player);
                    handleHorseCalling(player, foundHorse, distance, event);
                } else {
                    player.displayClientMessage(Component.literal("Your bound horse could not be found! It may be too far away or in an unloaded chunk."), true);
                    System.out.println("Bound horse not found in loaded chunks");
                }
            } else {
                player.displayClientMessage(Component.literal("Horn is not bound to any horse! Right-click a horse you own to bind it."), true);
            }
        }
    }

    public static BlockPos findSafeTeleportLocation(Level level, BlockPos playerPos, AbstractHorse horse) {
        // Try positions in a 3x3 area around the player
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Skip the exact player position
                if (x == 0 && z == 0) continue;

                BlockPos testPos = playerPos.offset(x, 0, z);

                // Check if this is a safe location for the horse
                if (isSafeTeleportLocation(level, testPos, horse)) {
                    return testPos;
                }

                // Also try one block up
                BlockPos testPosUp = testPos.above();
                if (isSafeTeleportLocation(level, testPosUp, horse)) {
                    return testPosUp;
                }
            }
        }

        return null; // No safe location found
    }

    private static boolean isSafeTeleportLocation(Level level, BlockPos pos, AbstractHorse horse) {
        // Check if the horse can fit (2 blocks high)
        BlockState groundState = level.getBlockState(pos.below());
        BlockState feetState = level.getBlockState(pos);
        BlockState headState = level.getBlockState(pos.above());

        // Ground must be solid
        if (!groundState.isSolid()) {
            return false;
        }

        // Feet and head positions must be passable
        if (!feetState.isAir() && !feetState.canBeReplaced()) {
            return false;
        }

        if (!headState.isAir() && !headState.canBeReplaced()) {
            return false;
        }

        // Don't teleport into lava or dangerous blocks
        if (feetState.liquid() || headState.liquid()) {
            return false;
        }

        return true;
    }

    public static void respawnHorseAtLocation(Level level, AbstractHorse originalHorse, BlockPos newPos) {
        // Save horse data to NBT
        CompoundTag horseData = new CompoundTag();
        originalHorse.save(horseData);

        // Get the horse type
        EntityType<?> horseType = originalHorse.getType();

        // Remove the original horse
        originalHorse.discard();

        // Create new horse at the target location
        AbstractHorse newHorse = (AbstractHorse) horseType.create(level);
        if (newHorse != null) {
            // Load the saved data into the new horse
            newHorse.load(horseData);

            // Set position and spawn
            newHorse.setPos(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);

            // Add to world
            level.addFreshEntity(newHorse);

            System.out.println("Horse respawned at: " + newPos);
        } else {
            System.out.println("Failed to create new horse entity");
        }
    }

    private static void handleHorseCalling(Player player, AbstractHorse horse, double distance, PlayerInteractEvent.RightClickItem event) {
        if (horse.isVehicle()) {
            player.displayClientMessage(Component.literal("Your horse is already being ridden!"), true);
            return;
        }

        // Find a safe teleport location near the player
        BlockPos playerPos = player.blockPosition();
        BlockPos teleportPos = findSafeTeleportLocation(player.level(), playerPos, horse);

        if (teleportPos != null) {
            double threshold = Config.HORSE_TELEPORT_DISTANCE_THRESHOLD.get();
            System.out.println("Distance check: " + distance + " blocks (threshold: " + threshold + ")");

            // Use configurable distance threshold
            if (distance > threshold) {
                System.out.println("Distance > " + threshold + " blocks, using despawn/spawn method");
                respawnHorseAtLocation(player.level(), horse, teleportPos);
            } else {
                // Use regular teleportation for shorter distances
                System.out.println("Distance <= " + threshold + " blocks, using regular teleportation");
                horse.teleportTo(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
                System.out.println("Horse teleported to: " + teleportPos);
            }

            player.displayClientMessage(Component.literal("Your horse has been called!"), true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else {
            System.out.println("No safe teleport location found, using navigation instead");
            // Fallback to navigation if teleport fails
            PathNavigation navigation = horse.getNavigation();
            navigation.moveTo(playerPos.getX(), playerPos.getY(), playerPos.getZ(), 1.5);

            player.displayClientMessage(Component.literal("Your horse is coming to you!"), true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    private static AbstractHorse findHorseByUUID(Level level, UUID horseUUID) {
        // Search through all loaded entities to find the horse with matching UUID
        if (level instanceof ServerLevel serverLevel) {
            for (var entity : serverLevel.getAllEntities()) {
                if (entity instanceof AbstractHorse horse && horse.getUUID().equals(horseUUID)) {
                    return horse;
                }
            }
        }
        return null;
    }
}