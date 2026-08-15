package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import dev.emi.emi.jemi.impl.JemiRecipeSlot.IngredientRenderer;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.OffsetDrawable;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.TankInfo;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;

public class JemiRecipeSlotBuilder {
	public final JemiIngredientAcceptor acceptor;
	public boolean large = false, defaultBackground = false;
	public int x, y;
	public int width = 18, height = 18;
	public Optional<String> name = Optional.empty();
	public ITooltipCallback<?> tooltipCallback;
	public OffsetDrawable background, overlay;
	public Map<IIngredientType<?>, IngredientRenderer<?>> renderers;
	public TankInfo tankInfo;

	public JemiRecipeSlotBuilder(RecipeIngredientRole role, int x, int y) {
		this.acceptor = new JemiIngredientAcceptor(role);
		this.x = x;
		this.y = y;
	}

	public <I> void addIngredients(IIngredientType<I> ingredientType, List<I> ingredients) {
		acceptor.addIngredients(ingredientType, ingredients);
	}

	public <I> void addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		acceptor.addIngredient(ingredientType, ingredient);
	}

	public void addIngredientsUnsafe(List<?> ingredients) {
		acceptor.addIngredientsUnsafe(ingredients);
	}

	public void addFluidStack(Fluid fluid, long amount) {
		acceptor.addFluidStack(fluid, amount);
	}

	public void addFluidStack(Fluid fluid, long amount, NBTTagCompound componentChanges) {
		acceptor.addFluidStack(fluid, amount, componentChanges);
	}

	public void addTooltipCallback(ITooltipCallback<?> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	public void setSlotName(String slotName) {
		name = Optional.ofNullable(slotName);
	}

	public void setBackground(IDrawable background, int xOffset, int yOffset) {
		this.background = new OffsetDrawable(background, xOffset, yOffset);
	}

	public void setOverlay(IDrawable overlay, int xOffset, int yOffset) {
		this.overlay = new OffsetDrawable(overlay, xOffset, yOffset);
	}

	public void setFluidRenderer(long capacity, boolean showCapacity, int width, int height) {
		this.tankInfo = new TankInfo(width, height, capacity, showCapacity);
	}

	public <T> void setCustomRenderer(IIngredientType<T> ingredientType,
			IIngredientRenderer<T> ingredientRenderer) {
		if (renderers == null) {
			renderers = Maps.newHashMap();
		}
		renderers.put(ingredientType, new IngredientRenderer<T>(ingredientType, ingredientRenderer));
	}

	public void addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		acceptor.addTypedIngredients(ingredients);
	}

	public void addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		acceptor.addOptionalTypedIngredients(ingredients);
	}

//	public void addRichTooltipCallback(IRecipeSlotRichTooltipCallback tooltipCallback) {
//		richTooltipCallback = tooltipCallback;
//	}

	public void addFluidStack(Fluid fluid) {
		acceptor.addFluidStack(fluid);
	}

	public void setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
	}

	public int getWidth() {
		return large ? 26 : 18;
	}

	public int getHeight() {
		return large ? 26 : 18;
	}

	public void setStandardSlotBackground() {
		this.defaultBackground = true;
	}

	public void setOutputSlotBackground() {
		this.defaultBackground = true;
		this.large = true;
	}
}
