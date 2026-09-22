package dev.emi.emi.mixin.jei;

import dev.emi.emi.jemi.JemiScreenSwitch;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.recipes.RecipesGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecipesGui.class, remap = false)
public class RecipesGuiMixin {
	@Inject(method = "handleKeybinds(I)Z", at = @At("HEAD"), cancellable = true)
	private void switchRecipeGui(int key, CallbackInfoReturnable<Boolean> cir) {
		if (KeyBindings.toggleOverlay.isActiveAndMatches(key) && JemiScreenSwitch.handle()) {
			cir.setReturnValue(true);
		}
	}
}
