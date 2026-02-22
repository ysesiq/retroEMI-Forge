package shim.net.minecraft.text;

import net.minecraft.util.EnumChatFormatting;

public interface MutableText extends Text {

	MutableText setStyle(Style s);
	MutableText formatted(EnumChatFormatting fmt);
	MutableText formatted(EnumChatFormatting... fmt);
	MutableText append(Text text);

}
