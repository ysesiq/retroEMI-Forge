package dev.emi.emi.recipe.forge;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiShapedRecipe;
import dev.emi.emi.runtime.ProxyRecipeManager;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.stream.Collectors;

public class EmiShapelessOreRecipe extends EmiCraftingRecipe {

	public EmiShapelessOreRecipe(ShapelessOreRecipe recipe) {
		super(recipe.getIngredients().stream().map(EmiShapedOreRecipe::fromOreInput).collect(Collectors.toList()),
			EmiStack.of(recipe.getRecipeOutput()), ProxyRecipeManager.getId(recipe));
		EmiShapedRecipe.setRemainders(input, recipe);
	}

	@Override
	public boolean canFit(int width, int height) {
		return input.size() <= width * height;
	}
}
