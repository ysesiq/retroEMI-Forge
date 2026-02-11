package shim.net.minecraft.util;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemDye;

public enum DyeColor {
	WHITE,
	ORANGE,
	MAGENTA,
	LIGHT_BLUE,
	YELLOW,
	LIME,
	PINK,
	GRAY,
	LIGHT_GRAY,
	CYAN,
	PURPLE,
	BLUE,
	BROWN,
	GREEN,
	RED,
	BLACK,
	;

	public float[] getColorComponents() {
		return EntitySheep.fleeceColorTable[BlockColored.func_150031_c(ordinal())];
	}

	public int getFireworkColor() {
		return ItemDye.field_150922_c[ordinal()];
	}
}
