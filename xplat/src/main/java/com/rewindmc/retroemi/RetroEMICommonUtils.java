package com.rewindmc.retroemi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class RetroEMICommonUtils {
    public static void offerOrDrop(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropItem(stack, false);
        }
    }

    public static boolean canCombine(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static String replaceCharAt(String s, int index, char c) {
        return s.substring(0, index) + c + s.substring(index + 1);
    }
}
