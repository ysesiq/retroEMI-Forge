package net.minecraft.client.gui.widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ButtonWidget extends ClickableWidget {
	public static final int DEFAULT_WIDTH_SMALL = 120;
	public static final int DEFAULT_WIDTH = 150;
	public static final int DEFAULT_HEIGHT = 20;
	protected static final NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> (MutableText) textSupplier.get();
	protected final PressAction onPress;
	protected final NarrationSupplier narrationSupplier;

	private final GuiButton delegate = new GuiButton(0, 0, 0, 0, 0, "");

	public static Builder builder(Text message, PressAction onPress) {
		return new Builder(message, onPress);
	}

	protected ButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, NarrationSupplier narrationSupplier) {
		super(x, y, width, height, message);
		this.onPress = onPress;
		this.narrationSupplier = narrationSupplier;
	}

	@Override
	public void renderWidget(DrawContext raw, int mouseX, int mouseY, float tickDelta) {
		delegate.xPosition = getX();
		delegate.yPosition = getY();
		delegate.width = width;
		delegate.height = height;
		delegate.displayString = getMessage().asString();
		delegate.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		onPress();
	}

	public void onPress() {
		this.onPress.onPress(this);
	}

	@SideOnly(Side.CLIENT)
	public static class Builder {
		private final Text message;
		private final PressAction onPress;
		@Nullable
		private List<TooltipComponent> tooltip;
		private int x;
		private int y;
		private int width = 150;
		private int height = 20;
		private NarrationSupplier narrationSupplier = DEFAULT_NARRATION_SUPPLIER;

		public Builder(Text message, PressAction onPress) {
			this.message = message;
			this.onPress = onPress;
		}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		public Builder width(int width) {
			this.width = width;
			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Builder dimensions(int x, int y, int width, int height) {
			return this.position(x, y).size(width, height);
		}

		public Builder tooltip(@Nullable List<TooltipComponent> tooltip) {
			this.tooltip = tooltip;
			return this;
		}

		public Builder narrationSupplier(NarrationSupplier narrationSupplier) {
			this.narrationSupplier = narrationSupplier;
			return this;
		}

		public ButtonWidget build() {
			ButtonWidget buttonWidget = new ButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier);
			buttonWidget.setTooltip(this.tooltip);
			return buttonWidget;
		}
	}

    @SideOnly(Side.CLIENT)
	public static interface PressAction {
		public void onPress(ButtonWidget var1);
	}

    @SideOnly(Side.CLIENT)
	public static interface NarrationSupplier {
		public MutableText createNarrationMessage(Supplier<MutableText> var1);
	}
}
