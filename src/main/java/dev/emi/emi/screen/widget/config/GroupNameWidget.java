package dev.emi.emi.screen.widget.config;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.config.ListWidget.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.Element;
import shim.net.minecraft.text.Text;
import net.minecraft.util.ResourceLocation;

public class GroupNameWidget extends Entry {
	protected static final Minecraft CLIENT = Minecraft.getMinecraft();
	public final String id;
	public final Text text;
	public final List<ConfigEntryWidget> children = Lists.newArrayList();
	public boolean collapsed = false;

	public GroupNameWidget(String id, Text text) {
		this.id = id;
		this.text = text;
	}

	@Override
	public void render(DrawContext raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.drawCenteredTextWithShadow(text, x + width / 2, y + 3, -1);
		if (hovered || collapsed) {
			String collapse = "[-]";
			int cx = x + width / 2 - CLIENT.fontRenderer.getStringWidth(text.asString()) / 2 - 20;
			if (collapsed) {
				collapse = "[+]";
			}
			context.drawTextWithShadow(EmiPort.literal(collapse), cx, y + 3, -1);
		}
	}

	@Override
	public int getHeight() {
		for (ConfigEntryWidget w : children) {
			if (w.isVisible()) {
				return 20;
			}
		}
		return 0;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isMouseOver(mouseX, mouseY)) {
			collapsed = !collapsed;
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0F));
			return true;
		}
		return false;
	}

	@Override
	public List<? extends Element> children() {
		return com.rewindmc.retroemi.shim.java.List.of();
	}
}
