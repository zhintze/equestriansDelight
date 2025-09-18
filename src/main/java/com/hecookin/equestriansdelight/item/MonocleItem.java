package com.hecookin.equestriansdelight.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MonocleItem extends ArmorItem {

    // Simple armor material for the monocle
    public static final ArmorMaterial MONOCLE_MATERIAL = new ArmorMaterial(
        Map.of(
            ArmorItem.Type.HELMET, 1  // Minimal armor value
        ),
        15, // Enchantability
        SoundEvents.ARMOR_EQUIP_LEATHER, // Equip sound
        () -> Ingredient.of(), // Repair ingredient (none)
        List.of(new ArmorMaterial.Layer(
            ResourceLocation.fromNamespaceAndPath("equestriansdelight", "monocle")
        )),
        0.0F, // Toughness
        0.0F  // Knockback resistance
    );

    public MonocleItem(Properties properties) {
        super(Holder.direct(MONOCLE_MATERIAL), ArmorItem.Type.HELMET, properties);
    }
}