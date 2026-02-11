package shim.net.minecraft.util;

public enum Formatting {
	BLACK('0'),
	DARK_BLUE('1'),
	DARK_GREEN('2'),
	DARK_AQUA('3'),
	DARK_RED('4'),
	DARK_PURPLE('5'),
	GOLD('6'),
	GRAY('7'),
	DARK_GRAY('8'),
	BLUE('9'),
	GREEN('a'),
	AQUA('b'),
	RED('c'),
	LIGHT_PURPLE('d'),
	YELLOW('e'),
	WHITE('f'),
	OBFUSCATED('k'),
	BOLD('l'),
	STRIKETHROUGH('m'),
	UNDERLINE('n'),
	ITALIC('o'),
	RESET('r');

	private final String code;

	Formatting(char c) {
		this.code = "§" + c;
	}

	@Override
	public String toString() {
		return code;
	}

	public Integer getColorValue() {
		if (this == BLACK) return 0x000000;
		else if (this == DARK_BLUE) return 0x0000AA;
		else if (this == DARK_GREEN) return 0x00AA00;
		else if (this == DARK_AQUA) return 0x00AAAA;
		else if (this == DARK_RED) return 0xAA0000;
		else if (this == DARK_PURPLE) return 0xAA00AA;
		else if (this == GOLD) return 0xFFAA00;
		else if (this == GRAY) return 0xAAAAAA;
		else if (this == DARK_GRAY) return 0x555555;
		else if (this == BLUE) return 0x5555FF;
		else if (this == GREEN) return 0x55FF55;
		else if (this == AQUA) return 0x55FFFF;
		else if (this == RED) return 0xFF5555;
		else if (this == LIGHT_PURPLE) return 0xFF55FF;
		else if (this == YELLOW) return 0x55FFFF;
		else if (this == WHITE) return 0xFFFFFF;
		else return null;
	}

}
