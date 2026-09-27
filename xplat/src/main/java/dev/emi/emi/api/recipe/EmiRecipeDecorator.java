package dev.emi.emi.api.recipe;

import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface EmiRecipeDecorator {

	void decorateRecipe(EmiRecipe recipe, WidgetHolder widgets);
}
