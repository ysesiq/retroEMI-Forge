package dev.emi.emi.screen;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.client.gui.DrawContext;

public class DisabledToast implements IToast {
	private static final ResourceLocation TEXTURE = EmiPort.id("textures/gui/toasts.png");

	@Override
	public Visibility draw(GuiToast manager, long time) {
		EmiDrawContext context = EmiDrawContext.wrap(DrawContext.INSTANCE);
		context.resetColor();
        DrawContext.INSTANCE.drawGuiTexture(TEXTURE, 0, 0, 160, 32);
		context.drawCenteredText(EmiPort.translatable("emi.disabled"), 30, 7);
		context.drawCenteredText(EmiConfig.toggleVisibility.getBindText(), 30, 18);
		if (time > 8_000 || EmiConfig.enabled) {
			return Visibility.HIDE;
		}
		return Visibility.SHOW;
	}
}
