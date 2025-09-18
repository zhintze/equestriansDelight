package com.hecookin.equestriansdelight;

import com.hecookin.equestriansdelight.gui.HorseStatsPanel;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = EquestriansDelight.MODID, value = Dist.CLIENT)
public class HorseStatsEventHandler {

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        /*if (event.getScreen() instanceof HorseInventoryScreen horseScreen) {
            HorseStatsPanel.renderStatsPanel(horseScreen, event.getGuiGraphics(), (int)event.getMouseX(), (int)event.getMouseY());
        }*/
    }

    @SubscribeEvent
    public static void onScreenMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (event.getScreen() instanceof HorseInventoryScreen horseScreen) {
            if (HorseStatsPanel.handleMouseScroll(horseScreen, (int)event.getMouseX(), (int)event.getMouseY(), event.getScrollDeltaY())) {
                event.setCanceled(true);
            }
        }
    }
}