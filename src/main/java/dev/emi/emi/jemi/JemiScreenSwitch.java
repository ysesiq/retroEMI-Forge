package dev.emi.emi.jemi;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.mixin.jei.accessor.RecipesGuiAccessor;
import dev.emi.emi.screen.RecipeScreen;
import dev.emi.emi.screen.WidgetGroup;
import mezz.jei.api.IRecipesGui;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import shim.mezz.jei.api.ingredients.ITypedIngredient;

public class JemiScreenSwitch {

	public static boolean handle() {
		if (Minecraft.getMinecraft().currentScreen instanceof RecipeScreen rs) {
			return toJei(rs);
		} else if (Minecraft.getMinecraft().currentScreen instanceof RecipesGui rg) {
			return toEmi(rg);
		}
		return false;
	}

	private static boolean toJei(RecipeScreen rs) {
		Object value = focusValue(rs);
		IRecipeCategory<?> category = getJeiCategory(rs.getFocusedCategory());
		return Optional.ofNullable(value).map(v -> {
			rs.close();
			IRecipesGui gui = JemiPlugin.runtime.getRecipesGui();
			gui.show(JemiPlugin.runtime.getRecipeRegistry().createFocus(IFocus.Mode.OUTPUT, v));
			Optional.ofNullable(category).ifPresent(c -> ((RecipesGuiAccessor) gui).getRecipeGuiLogic().setRecipeCategory(c));
			return true;
		}).orElse(false);
	}

	private static boolean toEmi(RecipesGui rg) {
		EmiStack stack = Optional.ofNullable(currentFocus(rg)).map(JemiUtil::getStack).filter(s -> !s.isEmpty()).orElse(EmiStack.EMPTY);
		EmiRecipeCategory category = Optional.ofNullable(((RecipesGuiAccessor) rg).getRecipeGuiLogic().getSelectedRecipeCategory()).map(JemiScreenSwitch::getEmiCategory).orElse(null);
		if (stack.isEmpty() && category == null) {
			return false;
		}
		rg.close();
		Optional.ofNullable(rg.getParentScreen()).filter(GuiContainer.class::isInstance).map(GuiContainer.class::cast)
			.ifPresent(Minecraft.getMinecraft()::displayGuiScreen);
		open(stack, category).ifPresent(rs -> Optional.ofNullable(category).ifPresent(rs::focusCategory));
		return true;
	}

	private static Optional<RecipeScreen> open(EmiStack stack, EmiRecipeCategory category) {
		if (stack.isEmpty()) {
			EmiApi.displayRecipeCategory(category);
		} else {
			EmiApi.displayRecipes(stack);
		}
		RecipeScreen rs = currentRecipeScreen();
		if (rs != null) {
			return Optional.of(rs);
		}
		EmiApi.displayAllRecipes();
		return Optional.ofNullable(currentRecipeScreen());
	}

	private static RecipeScreen currentRecipeScreen() {
		if (Minecraft.getMinecraft().currentScreen instanceof RecipeScreen rs) {
			return rs;
		}
		return null;
	}

	private static Object focusValue(RecipeScreen rs) {
		return Optional.ofNullable(currentRecipe(rs))
			.flatMap(r -> r.getOutputs().stream().filter(s -> !s.isEmpty()).findFirst())
			.flatMap(JemiUtil::getTyped)
			.map(t -> (Object) t.ingredient())
			.orElseGet(() -> Optional.of(rs.getHoveredStack())
				.filter(h -> !h.isEmpty())
				.map(h -> JemiUtil.getTyped(h.getEmiStacks().get(0)).map(t -> (Object) t.ingredient()).orElse(null))
				.orElse(null));
	}

	private static EmiRecipe currentRecipe(RecipeScreen rs) {
		return Optional.ofNullable(rs.getPage())
			.flatMap(groups -> groups.stream().map(g -> g.recipe).filter(Objects::nonNull).findFirst())
			.orElse(null);
	}

	private static Object currentFocus(RecipesGui rg) {
		List<RecipeLayout> layouts = ((RecipesGuiAccessor) rg).getRecipeLayouts();
		if (layouts != null) {
			return layouts.stream().map(RecipeLayout::getFocus).filter(Objects::nonNull)
				.map(f -> (Object) f.getValue()).findFirst().orElse(null);
		}
		return rg.getIngredientUnderMouse();
	}

	public static IRecipeCategory<?> getJeiCategory(EmiRecipeCategory category) {
		return JemiPlugin.CATEGORY_MAP.get(category);
	}

	public static EmiRecipeCategory getEmiCategory(IRecipeCategory<?> category) {
		return JemiPlugin.CATEGORY_MAP.entrySet().stream()
			.filter(e -> e.getValue() == category)
			.map(Map.Entry::getKey)
			.findFirst().orElse(null);
	}
}
