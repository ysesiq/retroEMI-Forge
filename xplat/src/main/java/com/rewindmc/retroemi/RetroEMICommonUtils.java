package com.rewindmc.retroemi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import dev.emi.emi.EmiPort;
import shim.net.minecraft.item.ItemStacks;

import java.util.ArrayList;
import java.util.List;

public class RetroEMICommonUtils {
    public static void offerOrDrop(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropPlayerItemWithRandomChoice(stack, false);
        }
    }

    public static boolean canCombine(ItemStack a, ItemStack b) {
        return a == null || b == null ? a == b : a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static String replaceCharAt(String s, int index, char c) {
        return s.substring(0, index) + c + s.substring(index + 1);
    }

    private static @Nullable String getIdInner(ItemStack stack) {
        if (ItemStacks.isEmpty(stack)) {
            return null;
        }
        Item item = stack.getItem();
        if (item instanceof ItemBlock ib) {
            return EmiPort.getBlockRegistry().getNameForObject(ib.field_150939_a);
        } else {
            return EmiPort.getItemRegistry().getNameForObject(item);
        }
    }

    public static @Nullable String getId(ItemStack stack) {
        String s = getIdInner(stack);
        if (s != null && s.contains(":")) {
            String[] parts = s.split(":");
            return parts[1];
        }
        return null;
    }
}
