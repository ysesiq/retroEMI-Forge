package dev.emi.emi.recipe;

import java.util.stream.Collectors;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class EmiShapelessRecipe extends EmiCraftingRecipe {

	public EmiShapelessRecipe(ShapelessRecipes recipe) {
		super(recipe.recipeItems.stream().map(i -> EmiIngredient.of(i)).collect(Collectors.toList()),
			EmiStack.of(EmiPort.getOutput(recipe)), EmiPort.getId(recipe));
		EmiShapedRecipe.setRemainders(input, recipe);
	}

	@Override
	public boolean canFit(int width, int height) {
		return input.size() <= width * height;
	}
}
