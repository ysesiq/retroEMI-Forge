package dev.emi.emi.nemi;

import java.util.List;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.Recipe;
import codechicken.nei.recipe.Recipe.RecipeId;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class NemiScreenSwitch {

	public static boolean handle() {
		if (Minecraft.getMinecraft().currentScreen instanceof RecipeScreen rs) {
			return toNei(rs);
		} else if (Minecraft.getMinecraft().currentScreen instanceof GuiRecipe<?> gr) {
			return toEmi(gr);
		}
		return false;
	}

	private static boolean toNei(RecipeScreen rs) {
		ItemStack stack = focusValue(rs);
		if (stack == null) {
			return false;
		}
		RecipeId recipeId = currentRecipeId(rs);
		rs.close();
		if (recipeId != null && GuiCraftingRecipe.openRecipeGui("recipeId", stack, recipeId)) {
			return true;
		}
		return GuiCraftingRecipe.openRecipeGui("item", stack);
	}

	private static boolean toEmi(GuiRecipe<?> gr) {
		ItemStack result = currentResult(gr);
		if (result == null) {
			return false;
		}
		EmiStack stack = EmiStack.of(result);
		if (stack.isEmpty()) {
			return false;
		}
		Minecraft.getMinecraft().displayGuiScreen(gr.getFirstScreenGeneral());
		return open(stack);
	}

	private static boolean open(EmiStack stack) {
		if (!stack.isEmpty()) {
			EmiApi.displayRecipes(stack);
		}
		if (currentRecipeScreen() == null) {
			EmiApi.displayAllRecipes();
		}
		return currentRecipeScreen() != null;
	}

	private static RecipeScreen currentRecipeScreen() {
		if (Minecraft.getMinecraft().currentScreen instanceof RecipeScreen rs) {
			return rs;
		}
		return null;
	}

	private static ItemStack focusValue(RecipeScreen rs) {
		EmiRecipe recipe = currentRecipe(rs);
		if (recipe != null) {
			for (EmiStack output : recipe.getOutputs()) {
				if (!output.isEmpty()) {
					ItemStack stack = output.getItemStack();
					if (stack != null) {
						return stack;
					}
				}
			}
		}
		EmiIngredient hovered = rs.getHoveredStack();
		if (hovered != null && !hovered.isEmpty() && !hovered.getEmiStacks().isEmpty()) {
			EmiStack stack = hovered.getEmiStacks().get(0);
			if (!stack.isEmpty()) {
				return stack.getItemStack();
			}
		}
		return null;
	}

	private static EmiRecipe currentRecipe(RecipeScreen rs) {
		return rs.getCurrentRecipe();
	}

	private static RecipeId currentRecipeId(RecipeScreen rs) {
		EmiRecipe recipe = currentRecipe(rs);
		if (recipe instanceof NemiRecipe nemi) {
			try {
				return nemi.getNeiRecipeId();
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	private static ItemStack currentResult(GuiRecipe<?> gr) {
		try {
			Recipe focused = gr.getFocusedRecipe();
			if (focused != null) {
				ItemStack result = focused.getResult();
				if (result != null) {
					return result;
				}
			}
			List<Integer> indices = gr.getRecipeIndices();
			if (!indices.isEmpty()) {
				PositionedStack ps = gr.getHandler().getResultStack(indices.get(0));
				if (ps != null && ps.item != null) {
					return ps.item;
				}
			}
		} catch (Exception ignored) {
		}
		return null;
	}
}
