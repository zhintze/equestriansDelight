package com.hecookin.equestriansdelight.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.hecookin.equestriansdelight.tooltip.HorseAttributeTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Optional;

public class AttributeOverlayHandler {

    private static final int BACKGROUND_COLOR = 0xFF100010;

    // Create tags for inspection equipment and inspectable entities
    private static final TagKey<Item> INSPECTION_EQUIPMENT_ITEM_TAG = TagKey.create(
            BuiltInRegistries.ITEM.key(),
            ResourceLocation.fromNamespaceAndPath("equestriansdelight", "inspection_equipment")
    );

    public static void renderAttributeOverlay(Minecraft minecraft, GuiGraphics guiGraphics, float partialTick) {
        isRenderingTooltipsAllowed(minecraft).ifPresent(abstractHorse -> {
            // Force rendering on top with explicit Z-depth control
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            // Push matrix to ensure we're rendering in front
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(0, 0, 400); // Move forward in Z-space to render on top

            actuallyRenderAttributeOverlay(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight(), abstractHorse, minecraft.font, minecraft.getItemRenderer());

            poseStack.popPose();
        });
    }

    private static Optional<LivingEntity> isRenderingTooltipsAllowed(Minecraft minecraft) {
        if (minecraft.options.hideGui) return Optional.empty();
        if (minecraft.options.getCameraType().isFirstPerson() && minecraft.crosshairPickEntity instanceof LivingEntity entity &&
            (entity instanceof AbstractHorse)) {
            if (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR && minecraft.cameraEntity instanceof Player player &&
                hasMonocleEquipped(player)) {
                if (player.getVehicle() != entity) {
                    return Optional.of(entity);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean hasMonocleEquipped(Player player) {
        // Check if player has monocle equipped in helmet slot
        return player.getInventory().getArmor(3).getItem() instanceof com.hecookin.equestriansdelight.item.MonocleItem;
    }

    private static void actuallyRenderAttributeOverlay(GuiGraphics guiGraphics, int screenWidth, int screenHeight, LivingEntity entity, Font font, ItemRenderer itemRenderer) {
        List<HorseAttributeTooltip> tooltipComponents = buildTooltipComponents(entity);
        if (tooltipComponents.isEmpty()) return;

        // Calculate unified background dimensions
        List<com.hecookin.equestriansdelight.tooltip.ClientHorseAttributeTooltip> clientTooltips =
            tooltipComponents.stream()
                .map(com.hecookin.equestriansdelight.tooltip.ClientHorseAttributeTooltip::new)
                .toList();

        int maxWidth = clientTooltips.stream()
            .mapToInt(tooltip -> tooltip.getWidth(font))
            .max()
            .orElse(0);

        int totalHeight = clientTooltips.stream()
            .mapToInt(tooltip -> tooltip.getHeight())
            .sum();

        int padding = 3; // Increased padding for better background fit
        int spacing = 1; // Increased spacing between tooltip entries for better readability
        totalHeight += (clientTooltips.size() + 2) * spacing; // Add spacing between tooltips

        // Position at top center of screen
        int posX = screenWidth / 2 - maxWidth / 2;  // Center horizontally
        int posY = 3;  // pixels from top of screen

        // Render unified background
        renderUnifiedBackground(guiGraphics, posX - padding, posY - padding,
                              maxWidth + padding * 2, totalHeight + padding * 2);

        // Render individual tooltips without their own backgrounds
        int currentY = posY;
        for (com.hecookin.equestriansdelight.tooltip.ClientHorseAttributeTooltip clientTooltip : clientTooltips) {
            renderTooltipContent(guiGraphics, font, posX, currentY, clientTooltip);
            currentY += clientTooltip.getHeight() + spacing;
        }
    }

    // Render unified background for all tooltips
    private static void renderUnifiedBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Outer border (dark)
        guiGraphics.fillGradient(x, y, x + width, y + 1, BACKGROUND_COLOR, BACKGROUND_COLOR); // Top
        guiGraphics.fillGradient(x, y + height - 1, x + width, y + height, BACKGROUND_COLOR, BACKGROUND_COLOR); // Bottom
        guiGraphics.fillGradient(x, y, x + 1, y + height, BACKGROUND_COLOR, BACKGROUND_COLOR); // Left
        guiGraphics.fillGradient(x + width - 1, y, x + width, y + height, BACKGROUND_COLOR, BACKGROUND_COLOR); // Right

        // Main background
        guiGraphics.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, BACKGROUND_COLOR, BACKGROUND_COLOR);

        // Inner gradient borders for that classic tooltip look
        guiGraphics.fillGradient(x + 1, y + 1, x + 2, y + height - 1, 0x505000FF, 0x5028007F);
        guiGraphics.fillGradient(x + width - 2, y + 1, x + width - 1, y + height - 1, 0x505000FF, 0x5028007F);
        guiGraphics.fillGradient(x + 1, y + 1, x + width - 1, y + 2, 0x505000FF, 0x505000FF);
        guiGraphics.fillGradient(x + 1, y + height - 2, x + width - 1, y + height - 1, 0x5028007F, 0x5028007F);
    }

    // Render tooltip content (text and icons) without background
    private static void renderTooltipContent(GuiGraphics guiGraphics, Font font, int x, int y,
                                           com.hecookin.equestriansdelight.tooltip.ClientHorseAttributeTooltip clientTooltip) {
        // Render the image (icon)
        clientTooltip.renderImage(font, x, y + 5, guiGraphics);

        // Render the text with proper buffer source
        var bufferSource = guiGraphics.bufferSource();
        var pose = guiGraphics.pose();
        pose.pushPose();
        clientTooltip.renderText(font, x, y, pose.last().pose(), bufferSource);
        pose.popPose();
        bufferSource.endBatch();
    }

    private static List<HorseAttributeTooltip> buildTooltipComponents(LivingEntity entity) {
        List<HorseAttributeTooltip> tooltipComponents = Lists.newArrayList();

        if (entity instanceof AbstractHorse horse) {
            // Use the same calculation method as inventory UI for consistency
            com.hecookin.equestriansdelight.HorseStatsData.Stats stats =
                com.hecookin.equestriansdelight.HorseStatsData.getHorseStats(horse);

            // Horse name or coat info at the top
            if (horse.hasCustomName()) {
                tooltipComponents.add(HorseAttributeTooltip.nameTooltip(horse.getCustomName().getString()));
            } else {
                tooltipComponents.add(HorseAttributeTooltip.nameTooltip(stats.variant));
            }

            // Health (all AbstractHorse entities have this)
            tooltipComponents.add(HorseAttributeTooltip.healthTooltip(stats.healthPoints, true));

            // Speed (all rideable entities should have movement speed)
            if (entity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)) {
                tooltipComponents.add(HorseAttributeTooltip.speedTooltip(stats.speedInternal, true));
            }

            // Jump (only horses, donkeys, mules have jump strength - llamas don't)
            if (entity.getAttributes().hasAttribute(Attributes.JUMP_STRENGTH)) {
                tooltipComponents.add(HorseAttributeTooltip.jumpHeightTooltip(stats.jumpInternal, true));
            }

            // Storage for entities that can carry items
            if (entity instanceof Llama llama) {
                // Llamas have natural storage based on strength
                tooltipComponents.add(HorseAttributeTooltip.strengthTooltip(llama.getStrength()));
            } else if (entity instanceof Donkey donkey) {
                // Donkeys only have storage when chest is equipped
                if (donkey.hasChest()) {
                    tooltipComponents.add(HorseAttributeTooltip.strengthTooltip(5)); // 15 slots when chest equipped
                }
            } else if (entity instanceof Mule mule) {
                // Mules only have storage when chest is equipped
                if (mule.hasChest()) {
                    tooltipComponents.add(HorseAttributeTooltip.strengthTooltip(5)); // 15 slots when chest equipped
                }
            }
        }

        return tooltipComponents;
    }
}