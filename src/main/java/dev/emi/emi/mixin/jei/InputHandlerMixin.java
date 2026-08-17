package dev.emi.emi.mixin.jei;

import dev.emi.emi.jemi.JemiScreenSwitch;
import mezz.jei.config.KeyBindings;
import mezz.jei.input.InputHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InputHandler.class, remap = false)
public class InputHandlerMixin {
	@Inject(method = "handleKeyDown(CI)Z", at = @At("HEAD"), cancellable = true)
	private void switchRecipeGui(char c, int eventKey, CallbackInfoReturnable<Boolean> cir) {
		if (KeyBindings.toggleOverlay.isActiveAndMatches(eventKey) && JemiScreenSwitch.handle()) {
			cir.setReturnValue(true);
		}
	}
}
