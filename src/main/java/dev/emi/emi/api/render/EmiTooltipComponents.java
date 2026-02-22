package dev.emi.emi.api.render;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import dev.emi.emi.screen.tooltip.RecipeCostTooltipComponent;
import dev.emi.emi.screen.tooltip.RecipeTooltipComponent;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.util.EnumChatFormatting;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.text.Text;

import java.util.List;

public class EmiTooltipComponents {

	/**
	 * @return A tooltip component that displays a provided recipe.
	 */
	public static TooltipComponent getRecipeTooltipComponent(EmiRecipe recipe) {
		return new RecipeTooltipComponent(recipe);
	}

	/**
	 * @return A tooltip component that displays the remainder of a provided ingredient.
	 */
	public static TooltipComponent getRemainderTooltipComponent(EmiIngredient ingredient) {
		return new RemainderTooltipComponent(ingredient);
	}

	/**
	 * @return A tooltip component that displays the the cost breakdown of a provided recipe.
	 */
	public static TooltipComponent getRecipeCostTooltipComponent(EmiRecipe recipe) {
		return new RecipeCostTooltipComponent(recipe);
	}

	/**
	 * @return A tooltip component that displays a collection of stacks to represent an ingredient
	 */
	public static TooltipComponent getIngredientTooltipComponent(List<? extends EmiIngredient> stacks) {
		return new IngredientTooltipComponent(stacks);
	}

	/**
	 * @return A tooltip component that displays the amount of a provided stack.
	 */
	public static TooltipComponent getAmount(EmiIngredient ingredient) {
		return of(EmiRenderHelper.getAmountText(ingredient, ingredient.getAmount()).copy().formatted(EnumChatFormatting.GRAY));
	}

	/**
	 * A shorthand to create a tooltip component from text
	 */
	public static TooltipComponent of(Text text) {
		return TooltipComponent.of(text.asOrderedText());
	}

	/**
	 * Appends a mod name to a list of components based on a namespace.
	 * Takes into consideration config options and formatting.
	 * EMI's config allows users to disable displaying mod names, so it is possible for the list of components to be unchanged.
	 */
	public static void appendModName(List<TooltipComponent> components, String namespace) {
		if (EmiConfig.appendModId) {
			String mod = EmiUtil.getModName(namespace);
			components.add(of(EmiPort.literal(mod, EnumChatFormatting.BLUE, EnumChatFormatting.ITALIC)));
		}
	}
}
