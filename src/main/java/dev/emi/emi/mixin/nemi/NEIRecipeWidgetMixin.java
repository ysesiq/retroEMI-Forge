package dev.emi.emi.mixin.nemi;

import codechicken.nei.recipe.GuiRecipeButton;
import codechicken.nei.recipe.NEIRecipeWidget;
import codechicken.nei.recipe.RecipeHandlerRef;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.llamalad7.mixinextras.sugar.Local;
import dev.emi.emi.nemi.GuiTreeButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = NEIRecipeWidget.class, remap = false)
public class NEIRecipeWidgetMixin {
	@Shadow @Final protected RecipeHandlerRef handlerRef;

	@Inject(method = "getDefaultButtons", at = @At("TAIL"))
	private void addTreeButton(CallbackInfoReturnable<List<GuiRecipeButton>> cir, @Local(name = "x") int x, @Local(name = "y") int y) {
		if (this.handlerRef.handler instanceof TemplateRecipeHandler) {
			List<GuiRecipeButton> buttons = cir.getReturnValue();
			buttons.add(new GuiTreeButton(this.handlerRef, x, y - GuiRecipeButton.BUTTON_HEIGHT - 1));
		}
	}
}
