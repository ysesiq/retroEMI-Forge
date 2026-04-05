package dev.emi.emi.mixin;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiToast.class)
public class GuiToastMixin {

	@Inject(at = @At("HEAD"), method = "drawToast", cancellable = true)
	private void drawHead(ScaledResolution resolution, CallbackInfo info) {
        Minecraft client = Minecraft.getMinecraft();
		if (client.currentScreen != null && EmiConfig.enabled && EmiApi.getHandledScreen() != null) {
			info.cancel();
		}
	}
}
