package shim.net.minecraft.client.gui.tooltip;

import net.minecraft.client.gui.FontRenderer;
import shim.net.minecraft.text.OrderedText;
import shim.net.minecraft.text.Text;

public interface TooltipComponent {
	static TooltipComponent of(Text text) {
		return new TextTooltipComponent(text.asString());
	}
	static TooltipComponent of(OrderedText text) {
		return new TextTooltipComponent(text.asString());
	}

	int getHeight();
	int getWidth(FontRenderer textRenderer);

	default void drawText(FontRenderer textRenderer, int x, int y) {}
	default void drawItems(FontRenderer textRenderer, int x, int y) {}
}
