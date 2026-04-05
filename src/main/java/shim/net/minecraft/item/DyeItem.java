package shim.net.minecraft.item;

import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;

public record DyeItem(EnumDyeColor color) {

	public ItemStack toStack() {
		return new ItemStack(Items.DYE, 1, color.getDyeDamage());
	}

	public static DyeItem byColor(EnumDyeColor color) {
		return new DyeItem(color);
	}

}
