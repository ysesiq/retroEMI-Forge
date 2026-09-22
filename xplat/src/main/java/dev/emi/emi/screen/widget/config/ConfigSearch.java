package dev.emi.emi.screen.widget.config;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import shim.net.minecraft.client.gui.widget.TextFieldWidget;
import shim.net.minecraft.text.Text;

public class ConfigSearch {
	public final ConfigSearchWidgetField field;

	public ConfigSearch(int x, int y, int width, int height) {
		Minecraft client = Minecraft.getMinecraft();

		field = new ConfigSearchWidgetField(client.fontRenderer, x, y, width, height, EmiPort.literal(""));
		field.setChangedListener(s -> {
			if (s.length() > 0) {
				field.setSuggestion("");
			} else {
				field.setSuggestion(RetroEMI.translate("emi.search_config"));
			}
		});
		field.setSuggestion(RetroEMI.translate("emi.search_config"));
	}

	public void setText(String query) {
		field.setText(query);
	}

	public String getSearch() {
		return field.getText();
	}

	public class ConfigSearchWidgetField extends TextFieldWidget {

		public ConfigSearchWidgetField(FontRenderer textRenderer, int x, int y, int width, int height, Text text) {
			super(textRenderer, x, y, width, height, text);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 1 && isMouseOver(mouseX, mouseY)) {
				this.setText("");
				EmiPort.focus(this, true);
				return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}
}
