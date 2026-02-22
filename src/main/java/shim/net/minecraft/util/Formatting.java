package shim.net.minecraft.util;

import net.minecraft.util.EnumChatFormatting;

public class Formatting {
	public static Integer getColorValue(EnumChatFormatting formatting) {
		if (formatting == EnumChatFormatting.BLACK) return 0x000000;
		else if (formatting == EnumChatFormatting.DARK_BLUE) return 0x0000AA;
		else if (formatting == EnumChatFormatting.DARK_GREEN) return 0x00AA00;
		else if (formatting == EnumChatFormatting.DARK_AQUA) return 0x00AAAA;
		else if (formatting == EnumChatFormatting.DARK_RED) return 0xAA0000;
		else if (formatting == EnumChatFormatting.DARK_PURPLE) return 0xAA00AA;
		else if (formatting == EnumChatFormatting.GOLD) return 0xFFAA00;
		else if (formatting == EnumChatFormatting.GRAY) return 0xAAAAAA;
		else if (formatting == EnumChatFormatting.DARK_GRAY) return 0x555555;
		else if (formatting == EnumChatFormatting.BLUE) return 0x5555FF;
		else if (formatting == EnumChatFormatting.GREEN) return 0x55FF55;
		else if (formatting == EnumChatFormatting.AQUA) return 0x55FFFF;
		else if (formatting == EnumChatFormatting.RED) return 0xFF5555;
		else if (formatting == EnumChatFormatting.LIGHT_PURPLE) return 0xFF55FF;
		else if (formatting == EnumChatFormatting.YELLOW) return 0x55FFFF;
		else if (formatting == EnumChatFormatting.WHITE) return 0xFFFFFF;
		else return 0xFFFFFF;
	}

}
