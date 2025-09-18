package com.hecookin.equestriansdelight;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import com.hecookin.equestriansdelight.client.AttributeOverlayHandler;
import com.hecookin.equestriansdelight.client.MonocleTooltipHandler;
import com.hecookin.equestriansdelight.tooltip.HorseAttributeTooltip;
import com.hecookin.equestriansdelight.tooltip.ClientHorseAttributeTooltip;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = EquestriansDelight.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = EquestriansDelight.MODID, value = Dist.CLIENT)
public class EquestriansDelightClient {
    public EquestriansDelightClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        EquestriansDelight.LOGGER.info("HELLO FROM CLIENT SETUP");
        EquestriansDelight.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

        // Register overlay handler for monocle tooltips
        NeoForge.EVENT_BUS.register(new Object() {
            @SubscribeEvent
            public void onRenderGui(RenderGuiEvent.Post event) {
                AttributeOverlayHandler.renderAttributeOverlay(
                    Minecraft.getInstance(),
                    event.getGuiGraphics(),
                    event.getPartialTick().getGameTimeDeltaTicks()
                );
            }

            @SubscribeEvent
            public void onItemTooltip(ItemTooltipEvent event) {
                MonocleTooltipHandler.onItemTooltip(
                    event.getItemStack(),
                    event.getToolTip(),
                    event.getContext(),
                    event.getEntity(),
                    event.getFlags()
                );
            }
        });
    }

    @SubscribeEvent
    static void onRegisterClientTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        // Register the client tooltip component for proper rendering
        event.register(HorseAttributeTooltip.class, ClientHorseAttributeTooltip::new);
    }
}