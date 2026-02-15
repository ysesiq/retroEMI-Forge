package dev.emi.emi.screen;

import java.lang.reflect.Field;
import java.util.List;

import shim.org.lwjgl.glfw.GLFW;

import com.rewindmc.retroemi.REMIScreen;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.com.unascribed.qdcss.QDCSS;
import dev.emi.emi.config.ConfigPresets;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.EmiConfig.ConfigGroup;
import dev.emi.emi.config.EmiConfig.ConfigValue;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.widget.config.EmiNameWidget;
import dev.emi.emi.screen.widget.config.ListWidget;
import net.minecraft.client.Minecraft;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.Element;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.gui.widget.ButtonWidget;
import shim.net.minecraft.text.Text;

public class ConfigPresetScreen extends REMIScreen {
	private final ConfigScreen last;
	private ListWidget list;
	public ButtonWidget resetButton;

	public ConfigPresetScreen(ConfigScreen last) {
		super(EmiPort.translatable("screen.emi.presets"));
		this.last = last;
	}

	@Override
	public void init() {
		super.init();
		this.addDrawable(new EmiNameWidget(width / 2, 16));
		int w = Math.min(400, width - 40);
		int x = (width - w) / 2;
		this.resetButton = EmiPort.newButton(x + 2, height - 30, w / 2 - 2, 20, EmiPort.translatable("gui.done"), button -> {
			EmiConfig.loadConfig(QDCSS.load("revert", last.originalConfig));
			Minecraft client = Minecraft.getMinecraft();
			setWorldAndResolution(client, width, height);
		});
		this.addDrawableChild(resetButton);
		this.addDrawableChild(EmiPort.newButton(x + w / 2 + 2, height - 30, w / 2 - 2, 20, EmiPort.translatable("gui.done"), button -> {
			this.close();
		}));
		list = new ListWidget(client, width, height, 40, height - 40);
		try {
			for (Field field : ConfigPresets.class.getFields()) {
				ConfigValue config = field.getDeclaredAnnotation(ConfigValue.class);
				if (config != null) {
					if (field.get(null) instanceof Runnable runnable) {
						ConfigGroup group = field.getDeclaredAnnotation(ConfigGroup.class);
						if (group != null) {
							Text translation = EmiPort.translatable("config.emi." + group.value().replace('-', '_'));
							list.addEntry(new PresetGroupWidget(translation));
						}
						Text translation = EmiPort.translatable("config.emi." + config.value().replace('-', '_'));
						list.addEntry(new PresetWidget(runnable, translation, ConfigScreen.getFieldTooltip(field)));
					}
				}
			}
		} catch (Exception e) {
		}
		this.addSelectableChild(list);
		updateChanges();
	}

	@Override
	public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		list.setScrollAmount(list.getScrollAmount());
		this.renderBackgroundTexture(context.raw());
		list.render(context.raw(), mouseX, mouseY, delta);
		super.render(context.raw(), mouseX, mouseY, delta);
		if (list.getHoveredEntry() instanceof PresetWidget widget) {
			if (widget.button.isHovered()) {
				EmiRenderHelper.drawTooltip(this, context, widget.tooltip, mouseX, mouseY);
			}
		}
	}

	@Override
	public void close() {
		Minecraft.getMinecraft().displayGuiScreen(last);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			this.close();
			return true;
		} else if (this.client.gameSettings.keyBindInventory.getKeyCode() == (keyCode)) {
			this.close();
			return true;
		} else if (keyCode == GLFW.GLFW_KEY_TAB) {
			return false;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public void updateChanges() {
		// Split on the blank lines between config options
		String[] oLines = last.originalConfig.split("\n\n");
		String[] cLines = EmiConfig.getSavedConfig().split("\n\n");
		int different = 0;
		for (int i = 0; i < oLines.length; i++) {
			if (i >= cLines.length) {
				break;
			}
			if (!oLines[i].equals(cLines[i])) {
				different++;
			}
		}
		this.resetButton.active = different > 0;
		this.resetButton.setMessage(EmiPort.translatable("screen.emi.config.reset", different));
	}

	public class PresetWidget extends ListWidget.Entry {
		private final ButtonWidget button;
		private final List<TooltipComponent> tooltip;

		public PresetWidget(Runnable runnable, Text name, List<TooltipComponent> tooltip) {
			button = EmiPort.newButton(0, 0, 200, 20, name, t -> {
				runnable.run();
				updateChanges();
			});
			this.tooltip = tooltip;
		}

		@Override
		public List<? extends Element> children() {
			return shim.java.List.of(button);
		}

		@Override
		public void render(DrawContext raw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
				boolean hovered, float delta) {
			button.y = y;
			button.x = x + width / 2 - button.getWidth() / 2;
			button.render(raw, mouseX, mouseY, delta);
		}

		@Override
		public int getHeight() {
			return 20;
		}
	}

	public class PresetGroupWidget extends ListWidget.Entry {
		private final Text text;

		public PresetGroupWidget(Text text) {
			this.text = text;
		}

		@Override
		public List<? extends Element> children() {
			return shim.java.List.of();
		}

		@Override
		public void render(DrawContext raw, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			context.drawCenteredTextWithShadow(text, x + width / 2, y + 3, -1);
		}

		@Override
		public int getHeight() {
			return 20;
		}
	}
}
