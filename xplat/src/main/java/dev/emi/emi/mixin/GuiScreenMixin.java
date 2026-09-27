package dev.emi.emi.mixin;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenBase;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class GuiScreenMixin {
	@Shadow public Minecraft mc;

	@Inject(method = "handleMouseInput", at = @At("TAIL"), cancellable = true)
	private void handleEmiMouseInput(CallbackInfo ci) {
		if (!RetroEMI.hasFocusedTextReflectField(this)) {
			if (RetroEMI.handleMouseInput()) {
				ci.cancel();
			}
		}
	}

//	@Inject(method = "handleKeyboardInput", at = @At("TAIL"), cancellable = true)
//	private void handleEmiKeyboardInput(CallbackInfo ci) {
//		if (!RetroEMI.hasFocusedTextReflectField(this)) {
//			if (RetroEMI.handleKeyboardInput()) {
//				ci.cancel();
//			}
//		}
//	}

	@Inject(method = "drawDefaultBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawWorldBackground(I)V", shift = At.Shift.AFTER))
	private void drawEmiBackground(CallbackInfo ci) {
		final ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		final int scaledWidth = scaledresolution.getScaledWidth();
		final int scaledHeight = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaledWidth / this.mc.displayWidth;
		int mouseY = scaledHeight - Mouse.getY() * scaledHeight / this.mc.displayHeight - 1;

		EmiDrawContext context = EmiDrawContext.instance();
		GuiScreen screen = (GuiScreen) (Object) this;
		if (!(screen instanceof GuiContainer)) {
			return;
		}
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			EmiScreenManager.drawBackground(context, mouseX, mouseY, this.mc.timer.renderPartialTicks);
		}
	}
}
