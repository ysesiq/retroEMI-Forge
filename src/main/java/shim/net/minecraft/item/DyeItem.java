package shim.net.minecraft.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import shim.net.minecraft.util.DyeColor;

public record DyeItem(DyeColor color) {

	public ItemStack toStack() {
		return new ItemStack(Items.DYE, 1, color.ordinal());
	}

	public static DyeItem byColor(DyeColor color) {
		return new DyeItem(color);
	}

}
