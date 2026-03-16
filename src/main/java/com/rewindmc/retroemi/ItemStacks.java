package com.rewindmc.retroemi;

import net.minecraft.item.ItemStack;

public class ItemStacks {

	public static final ItemStack EMPTY = null;

	public static boolean isEmpty(ItemStack stack) {
		return stack == null || stack.getCount() == 0 || stack.getItem() == null ? true : stack.getItem().delegate == null;
	}

}
