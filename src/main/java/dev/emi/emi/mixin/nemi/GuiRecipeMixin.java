package dev.emi.emi.mixin.nemi;

import codechicken.nei.recipe.GuiRecipe;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.nemi.NemiScreenSwitch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiRecipe.class, remap = false)
public class GuiRecipeMixin {
	@Inject(method = "keyTyped(CI)V", at = @At("HEAD"), cancellable = true)
	private void switchRecipeGui(char c, int i, CallbackInfo ci) {
		if (EmiConfig.toggleVisibility.matchesKey(i, -1) && NemiScreenSwitch.handle()) {
			ci.cancel();
		}
	}
}
