package com.hecookin.equestriansdelight.client;

import com.hecookin.equestriansdelight.EquestriansDelight;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = EquestriansDelight.MODID, value = Dist.CLIENT)
public class HornTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        // Check if it's a goat horn
        if (stack.getItem() == Items.GOAT_HORN) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag hornTag = customData.copyTag();

            if (hornTag.contains("BoundHorseUUID")) {
                String horseName = hornTag.getString("BoundHorseName");
                if (!horseName.isEmpty()) {
                    // Add the bound horse name to the tooltip
                    event.getToolTip().add(Component.literal("§7Bound to: §f" + horseName));
                    event.getToolTip().add(Component.literal("§8Right-click to call your horse"));
                } else {
                    event.getToolTip().add(Component.literal("§7Bound to a horse"));
                    event.getToolTip().add(Component.literal("§8Right-click to call your horse"));
                }
            } else {
                // Horn is not bound
                event.getToolTip().add(Component.literal("§8Right-click a horse you own to bind"));
            }
        }
    }
}