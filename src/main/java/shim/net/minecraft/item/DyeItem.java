package shim.net.minecraft.item;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import shim.net.minecraft.util.DyeColor;

@Desugar
public record DyeItem(DyeColor color) {

	public ItemStack toStack() {
		return new ItemStack(Items.dye, 1, color.ordinal());
	}

	public static DyeItem byColor(DyeColor color) {
		return new DyeItem(color);
	}

}
