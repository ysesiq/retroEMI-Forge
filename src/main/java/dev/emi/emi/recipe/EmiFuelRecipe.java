package dev.emi.emi.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

public class EmiFuelRecipe implements EmiRecipe {
	private final EmiIngredient stack;
	private final int time;
	private final ResourceLocation id;

	public EmiFuelRecipe(EmiIngredient stack, int time, ResourceLocation id) {
		this.stack = stack;
		this.time = time;
		this.id = id;
		if (stack.getEmiStacks().get(0).getItemStack().getItem().equals(Items.lava_bucket)) {
			stack.getEmiStacks().get(0).setRemainder(EmiStack.of(Items.bucket));
		}
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.FUEL;
	}

	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return shim.java.List.of(stack);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return shim.java.List.of();
	}

	@Override
	public int getDisplayWidth() {
		return 144;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_FLAME, 1, 1);
		widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 1, 1, 1000 * time / 20, false, true, true);
		widgets.addSlot(stack, 18, 0).recipeContext(this);
		widgets.addText(EmiPort.translatable("emi.fuel_time.items",
			EmiRenderHelper.TEXT_FORMAT.format(time / 200f)), 38, 5, -1, true);
	}
}
