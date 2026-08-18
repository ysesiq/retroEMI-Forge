package dev.emi.emi.recipe;

import java.util.List;
import java.util.stream.Collectors;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.ProxyRecipeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;

public class EmiShapelessRecipe extends EmiCraftingRecipe {

	public EmiShapelessRecipe(ShapelessRecipes recipe) {
		super((List<EmiIngredient>) recipe.recipeItems.stream().map(i -> EmiStack.of((ItemStack) i)).collect(Collectors.toList()),
			EmiStack.of(EmiPort.getOutput(recipe)), ProxyRecipeManager.getId(recipe));
		EmiShapedRecipe.setRemainders(input, recipe);
	}

	@Override
	public boolean canFit(int width, int height) {
		return input.size() <= width * height;
	}
}
