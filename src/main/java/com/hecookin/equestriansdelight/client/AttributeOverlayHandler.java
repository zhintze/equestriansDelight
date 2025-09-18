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
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Optional;

public class AttributeOverlayHandler {

    // Create tags for inspection equipment and inspectable entities
    private static final TagKey<Item> INSPECTION_EQUIPMENT_ITEM_TAG = TagKey.create(
            BuiltInRegistries.ITEM.key(),
            ResourceLocation.fromNamespaceAndPath("equestriansdelight", "inspection_equipment")
    );

    public static void renderAttributeOverlay(Minecraft minecraft, GuiGraphics guiGraphics, float partialTick) {
        isRenderingTooltipsAllowed(minecraft).ifPresent(abstractHorse -> {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            actuallyRenderAttributeOverlay(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight(), abstractHorse, minecraft.font, minecraft.getItemRenderer());
        });
    }

    private static Optional<LivingEntity> isRenderingTooltipsAllowed(Minecraft minecraft) {
        if (minecraft.options.hideGui) return Optional.empty();
        if (minecraft.options.getCameraType().isFirstPerson() && minecraft.crosshairPickEntity instanceof LivingEntity entity &&
            (entity instanceof AbstractHorse)) {
            if (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR && minecraft.cameraEntity instanceof Player player &&
                hasMonocleEquipped(player)) {
                if (player.getVehicle() != entity && (!(entity instanceof AbstractHorse abstractHorse) || abstractHorse.isTamed())) {
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

        // Position the unified background
        int posX = screenWidth / 2 - 12 + 22;
        int posY = screenHeight / 2 - 15 - totalHeight / 2;

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
        guiGraphics.fillGradient(x, y, x + width, y + 1, 0xF0100010, 0xF0100010); // Top
        guiGraphics.fillGradient(x, y + height - 1, x + width, y + height, 0xF0100010, 0xF0100010); // Bottom
        guiGraphics.fillGradient(x, y, x + 1, y + height, 0xF0100010, 0xF0100010); // Left
        guiGraphics.fillGradient(x + width - 1, y, x + width, y + height, 0xF0100010, 0xF0100010); // Right

        // Main background
        guiGraphics.fillGradient(x + 1, y + 1, x + width - 1, y + height - 1, 0xF0100010, 0xF0100010);

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

            // Health
            tooltipComponents.add(HorseAttributeTooltip.healthTooltip(stats.healthPoints, true));

            // Skip speed/jump for llamas
            if (!(entity instanceof Llama)) {
                // Speed
                tooltipComponents.add(HorseAttributeTooltip.speedTooltip(stats.speedInternal, true));
                // Jump - use the properly calculated jump strength from inventory UI
                tooltipComponents.add(HorseAttributeTooltip.jumpHeightTooltip(stats.jumpInternal, true));
            }
        } else if (entity instanceof Llama llama) {
            // Handle llamas separately
            if (entity.getAttributes().hasAttribute(Attributes.MAX_HEALTH)) {
                tooltipComponents.add(HorseAttributeTooltip.healthTooltip(entity.getAttributeValue(Attributes.MAX_HEALTH), false));
            }
            tooltipComponents.add(HorseAttributeTooltip.strengthTooltip(llama.getStrength()));
        }

        return tooltipComponents;
    }
}