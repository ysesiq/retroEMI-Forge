package shim.net.minecraft.util;

import net.minecraft.util.text.TextFormatting;

public class Formatting {
	public static Integer getColorValue(TextFormatting formatting) {
		if (formatting == TextFormatting.BLACK) return 0x000000;
		else if (formatting == TextFormatting.DARK_BLUE) return 0x0000AA;
		else if (formatting == TextFormatting.DARK_GREEN) return 0x00AA00;
		else if (formatting == TextFormatting.DARK_AQUA) return 0x00AAAA;
		else if (formatting == TextFormatting.DARK_RED) return 0xAA0000;
		else if (formatting == TextFormatting.DARK_PURPLE) return 0xAA00AA;
		else if (formatting == TextFormatting.GOLD) return 0xFFAA00;
		else if (formatting == TextFormatting.GRAY) return 0xAAAAAA;
		else if (formatting == TextFormatting.DARK_GRAY) return 0x555555;
		else if (formatting == TextFormatting.BLUE) return 0x5555FF;
		else if (formatting == TextFormatting.GREEN) return 0x55FF55;
		else if (formatting == TextFormatting.AQUA) return 0x55FFFF;
		else if (formatting == TextFormatting.RED) return 0xFF5555;
		else if (formatting == TextFormatting.LIGHT_PURPLE) return 0xFF55FF;
		else if (formatting == TextFormatting.YELLOW) return 0x55FFFF;
		else if (formatting == TextFormatting.WHITE) return 0xFFFFFF;
		else return 0xFFFFFF;
	}

}
