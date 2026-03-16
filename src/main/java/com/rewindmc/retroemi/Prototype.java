package com.rewindmc.retroemi;

import dev.emi.emi.EmiPort;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public record Prototype(Item item, int meta) {

	public static final Prototype EMPTY = new Prototype(null);

	public Prototype(Item item) {
		this(item, 0);
	}

	public ItemStack toStack() {
		return toStack(1);
	}

	public ItemStack toStack(int count) {
		if (item == null) return null;
		return new ItemStack(item, count, meta);
	}

	public Item getItem() {
		return item();
	}

	public String toString() {
		if (item == null) return "Prototype[EMPTY]";
		return "Prototype[" + item.getItemStackDisplayName(new ItemStack(item)) + "(" + EmiPort.getItemRegistry().getIDForObject(item) + "):" + meta + "]";
	}


	public static Prototype of(ItemStack stack) {
		if (stack == null) return EMPTY;
		return new Prototype(stack.getItem(), stack.getItemDamage());
	}

	public int hashCode() {
		return 10;
	}
}
