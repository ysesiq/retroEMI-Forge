package dev.emi.emi.mixin;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen {
	@Shadow public Container inventorySlots;

	@Inject(method = "initGui", at = @At("TAIL"))
	private void addEMIWidgets(CallbackInfo ci) {
		EmiScreenManager.addWidgets(this);
	}

	@Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
	public void disableHotkeyInEMISearchInput(char c, int k, CallbackInfo ci) {
		if (RetroEMI.handleKeyboardInput()) {
			ci.cancel();
		}
	}

	@Inject(method = "drawScreen", at = @At("RETURN"))
	private void emiRender(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		EmiDrawContext context = EmiDrawContext.instance();
		context.push();
		EmiPort.setPositionTexShader();
		EmiScreenManager.render(context, mouseX, mouseY, partialTicks);
		context.pop();
	}
}
