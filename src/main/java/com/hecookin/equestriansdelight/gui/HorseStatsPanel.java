package com.hecookin.equestriansdelight.gui;

import com.hecookin.equestriansdelight.HorseStatsData;
import com.hecookin.equestriansdelight.tooltip.HorseAttributeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

import java.lang.reflect.Field;

public class HorseStatsPanel {
    private static int scrollOffset = 0;
    private static final int MAX_VISIBLE_LINES = 7; // Adjusted for 60px height with normal font
    private static final int LINE_HEIGHT = 10;
    private static final int PANEL_WIDTH = 90;
    private static final int PANEL_HEIGHT = 75; // Align with horse display box height
    private static final int BACKGROUND_COLOR = 0xFF000000;

    public static void renderStatsPanel(HorseInventoryScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
        try {
            AbstractHorse horse = getHorseFromScreen(screen);
            if (horse == null) return;

            // Calculate panel position (in the empty area to the right of horse)
            int guiLeft = (screen.width - 176) / 2;
            int guiTop = (screen.height - 190) / 2;
            int panelX = guiLeft + 79;  // Start after horse area
            int panelY = guiTop + 18;  // Align with horse area top

            // Draw panel background with border (like vanilla GUI elements)
            drawPanelBackground(guiGraphics, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

            // Get stats and render them with proper clipping
            HorseStatsData.Stats stats = HorseStatsData.getHorseStats(horse);

            // Enable scissor (clipping) to prevent text from rendering outside panel
            guiGraphics.enableScissor(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT);
            renderStatsContent(guiGraphics, screen, panelX + 4, panelY + 4, horse, stats);
            guiGraphics.disableScissor();

            // Draw scroll indicator if needed
            if (getTotalLines() > MAX_VISIBLE_LINES) {
                drawScrollIndicator(guiGraphics, panelX + PANEL_WIDTH - 5, panelY + 2, PANEL_HEIGHT - 3);
            }

        } catch (Exception e) {
            // Silently fail if reflection doesn't work
        }
    }

    public static boolean handleMouseScroll(HorseInventoryScreen screen, int mouseX, int mouseY, double scrollDelta) {
        try {
            // Check if mouse is over the stats panel
            int guiLeft = (screen.width - 176) / 2;
            int guiTop = (screen.height - 146) / 2;
            int panelX = guiLeft + 79;
            int panelY = guiTop + 18;

            if (mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH &&
                mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT) {

                int totalLines = getTotalLines();
                if (totalLines > MAX_VISIBLE_LINES) {
                    scrollOffset -= (int) scrollDelta;
                    scrollOffset = Math.max(0, Math.min(scrollOffset, totalLines - MAX_VISIBLE_LINES));
                    return true; // Consumed the scroll event
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        return false;
    }

    private static void drawPanelBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Draw background similar to vanilla inventory slots
        guiGraphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);

        // Draw border
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF555555); // Top border
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF555555); // Left border
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF); // Right border (light)
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF); // Bottom border (light)
    }

    private static void renderStatsContent(GuiGraphics guiGraphics, HorseInventoryScreen screen, int x, int y, AbstractHorse horse, HorseStatsData.Stats stats) {
        int currentLine = 0;
        int currentY = y;


        // Health
        if (shouldRenderLine(currentLine++)) {
            Component healthLabel = Component.literal("Health:").withStyle(ChatFormatting.GRAY);
            guiGraphics.drawString(screen.getMinecraft().font, healthLabel, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }
        if (shouldRenderLine(currentLine++)) {
            Component healthValue = Component.literal(String.format("  %s hearts", HorseAttributeTooltip.formatValue(stats.hearts)))
                .withStyle(ChatFormatting.RED);
            guiGraphics.drawString(screen.getMinecraft().font, healthValue, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }

        // Speed
        if (shouldRenderLine(currentLine++)) {
            Component speedLabel = Component.literal("Speed:").withStyle(ChatFormatting.GRAY);
            guiGraphics.drawString(screen.getMinecraft().font, speedLabel, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }
        if (shouldRenderLine(currentLine++)) {
            Component speedValue = Component.literal(String.format("  %s b/s", HorseAttributeTooltip.formatValue(stats.speedBlocksPerSec)))
                .withStyle(ChatFormatting.GREEN);
            guiGraphics.drawString(screen.getMinecraft().font, speedValue, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }

        // Jump
        if (shouldRenderLine(currentLine++)) {
            Component jumpLabel = Component.literal("Jump:").withStyle(ChatFormatting.GRAY);
            guiGraphics.drawString(screen.getMinecraft().font, jumpLabel, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }
        if (shouldRenderLine(currentLine++)) {
            Component jumpValue = Component.literal(String.format("  %s blocks", HorseAttributeTooltip.formatValue(stats.jumpBlocksApprox)))
                .withStyle(ChatFormatting.AQUA);
            guiGraphics.drawString(screen.getMinecraft().font, jumpValue, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }

        // Coat
        if (shouldRenderLine(currentLine++)) {
            Component coatLabel = Component.literal("Coat:").withStyle(ChatFormatting.GRAY);
            guiGraphics.drawString(screen.getMinecraft().font, coatLabel, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }

        // Handle multi-line coat names
        String coatText = stats.variant;
        String[] words = coatText.split(" ");
        StringBuilder currentLineText = new StringBuilder("  ");

        for (String word : words) {
            // Check if adding this word would exceed reasonable width (about 12 chars)
            if (currentLineText.length() + word.length() + 1 > 12 && currentLineText.length() > 2) {
                // Render current line and start new one
                if (shouldRenderLine(currentLine++)) {
                    Component coatValue = Component.literal(currentLineText.toString()).withStyle(ChatFormatting.GOLD);
                    guiGraphics.drawString(screen.getMinecraft().font, coatValue, x, currentY, 0xFFFFFF, false);
                    currentY += LINE_HEIGHT;
                }
                currentLineText = new StringBuilder("  " + word);
            } else {
                if (currentLineText.length() > 2) currentLineText.append(" ");
                currentLineText.append(word);
            }
        }

        // Render final line
        if (currentLineText.length() > 2 && shouldRenderLine(currentLine++)) {
            Component coatValue = Component.literal(currentLineText.toString()).withStyle(ChatFormatting.GOLD);
            guiGraphics.drawString(screen.getMinecraft().font, coatValue, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }

        // Genetics section (future breeding system)
        if (shouldRenderLine(currentLine++)) {
            Component geneticsLabel = Component.literal("Genetics:").withStyle(ChatFormatting.DARK_GRAY);
            guiGraphics.drawString(screen.getMinecraft().font, geneticsLabel, x, currentY, 0xFFFFFF, false);
            currentY += LINE_HEIGHT;
        }
        if (shouldRenderLine(currentLine++)) {
            Component geneticsValue = Component.literal("  Coming Soon...").withStyle(ChatFormatting.DARK_GRAY);
            guiGraphics.drawString(screen.getMinecraft().font, geneticsValue, x, currentY, 0xFFFFFF, false);
        }
    }

    private static boolean shouldRenderLine(int lineIndex) {
        return lineIndex >= scrollOffset && lineIndex < scrollOffset + MAX_VISIBLE_LINES;
    }

    private static int getTotalLines() {
        return 11; // Reduced total lines (removed title, status and breeding sections)
    }

    private static void drawScrollIndicator(GuiGraphics guiGraphics, int x, int y, int height) {
        int totalLines = getTotalLines();
        int scrollBarHeight = Math.max(10, (MAX_VISIBLE_LINES * height) / totalLines);
        int scrollBarY = y + (scrollOffset * (height - scrollBarHeight)) / (totalLines - MAX_VISIBLE_LINES);

        // Draw scroll track
        guiGraphics.fill(x+1, y, x + 3, y + height, 0xFF333333);

        // Draw scroll thumb
        guiGraphics.fill(x + 1, scrollBarY, x + 3, scrollBarY + scrollBarHeight, 0xFF999999);
    }

    private static AbstractHorse getHorseFromScreen(HorseInventoryScreen screen) {
        try {
            // Use reflection to access the horse field from HorseInventoryScreen
            Field[] fields = HorseInventoryScreen.class.getDeclaredFields();
            for (Field field : fields) {
                if (AbstractHorse.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    return (AbstractHorse) field.get(screen);
                }
            }

            // Alternative: try accessing through the menu
            if (screen.getMenu() != null) {
                Field[] menuFields = screen.getMenu().getClass().getSuperclass().getDeclaredFields();
                for (Field field : menuFields) {
                    if (AbstractHorse.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        return (AbstractHorse) field.get(screen.getMenu());
                    }
                }
            }
        } catch (Exception e) {
            // Return null if reflection fails
        }
        return null;
    }
}