package shim.net.minecraft.text;

import net.minecraft.util.text.TextFormatting;

public interface MutableText extends Text {

	MutableText setStyle(Style s);
	MutableText formatted(TextFormatting fmt);
	MutableText formatted(TextFormatting... fmt);
	MutableText append(Text text);

}
