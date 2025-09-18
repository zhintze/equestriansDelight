package com.hecookin.equestriansdelight.client;

import com.hecookin.equestriansdelight.item.MonocleItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MonocleTooltipHandler {
    public static final Component MONOCLE_TOOLTIP_COMPONENT = Component.translatable("item.equestriansdelight.monocle.tooltip").withStyle(ChatFormatting.BLUE);

    public static void onItemTooltip(ItemStack itemStack, List<Component> lines, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
        if (itemStack.getItem() instanceof MonocleItem) {
            if (tooltipFlag.isAdvanced()) {
                int index = lines.size() - (!itemStack.getComponents().isEmpty() ? 2 : 1);
                lines.add(index, MONOCLE_TOOLTIP_COMPONENT);
            } else {
                lines.add(MONOCLE_TOOLTIP_COMPONENT);
            }
        }
    }
}