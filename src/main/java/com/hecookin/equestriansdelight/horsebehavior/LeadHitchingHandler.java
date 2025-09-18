package com.hecookin.equestriansdelight.horsebehavior;

import com.hecookin.equestriansdelight.Config;
import com.hecookin.equestriansdelight.EquestriansDelight;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = EquestriansDelight.MODID)
public class LeadHitchingHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!Config.ENABLE_LEAD_HITCHING.get()) {
            return;
        }

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        InteractionHand hand = event.getHand();
        ItemStack itemInHand = player.getItemInHand(hand);

        // Check if player is riding a horse and has a lead in inventory
        if (player.isPassenger() && player.getVehicle() instanceof AbstractHorse horse) {
            // Check if clicking on a fence or fence gate
            if (level.getBlockState(pos).getBlock() instanceof FenceBlock ||
                level.getBlockState(pos).getBlock() instanceof FenceGateBlock) {

                // Check if player has a lead in their inventory
                boolean hasLead = false;
                ItemStack leadStack = null;

                // First check the item in hand
                if (itemInHand.is(Items.LEAD)) {
                    hasLead = true;
                    leadStack = itemInHand;
                } else {
                    // Check inventory for lead
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.is(Items.LEAD)) {
                            hasLead = true;
                            leadStack = stack;
                            break;
                        }
                    }
                }

                if (hasLead && leadStack != null) {
                    // Dismount the player
                    player.stopRiding();

                    // Create or get fence knot entity at the position
                    LeashFenceKnotEntity fenceKnot = LeashFenceKnotEntity.getOrCreateKnot(level, pos);

                    // Set the leash on the horse to the fence knot
                    horse.setLeashedTo(fenceKnot, true);

                    // Consume the lead
                    leadStack.shrink(1);

                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }
}