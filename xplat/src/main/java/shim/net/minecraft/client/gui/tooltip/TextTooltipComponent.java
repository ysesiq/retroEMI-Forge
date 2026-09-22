package shim.net.minecraft.client.gui.tooltip;

import net.minecraft.client.gui.FontRenderer;

public class TextTooltipComponent implements TooltipComponent {
	private final String text;

	public TextTooltipComponent(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public int getWidth(FontRenderer textRenderer) {
		return textRenderer.getStringWidth(this.text);
	}

	@Override
	public int getHeight() {
		return 10;
	}

	@Override
	public void drawText(FontRenderer textRenderer, int x, int y) {
		textRenderer.drawStringWithShadow(this.text, x, y, -1);
	}
}
