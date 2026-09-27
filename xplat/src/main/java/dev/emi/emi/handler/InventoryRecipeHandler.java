package dev.emi.emi.handler;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.mixin.accessor.InventoryCraftingAccessor;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.text.Text;

public class InventoryRecipeHandler implements StandardRecipeHandler<ContainerPlayer> {
	public static final Text TOO_SMALL = EmiPort.translatable("emi.too_small");

	@Override
	public List<Slot> getInputSources(ContainerPlayer handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 5; i++) {
			list.add(handler.getSlot(i));
		}
		int invStart = 9;
		for (int i = invStart; i < invStart + 36; i++) {
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(ContainerPlayer handler) {
		List<Slot> list = Lists.newArrayList();
		// This is like, bad, right? There has to be a better way to do this
		list.add(handler.getSlot(1));
		list.add(handler.getSlot(2));
		list.add(null);
		list.add(handler.getSlot(3));
		list.add(handler.getSlot(4));
		list.add(null);
		list.add(null);
		list.add(null);
		list.add(null);
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(EmiRecipe recipe, ContainerPlayer handler) {
		if (recipe instanceof EmiCraftingRecipe craf && craf.shapeless) {
			List<Slot> list = Lists.newArrayList();
			list.add(handler.getSlot(1));
			list.add(handler.getSlot(2));
			list.add(handler.getSlot(3));
			list.add(handler.getSlot(4));
			return list;
		}
		return getCraftingSlots(handler);
	}

	@Override
	public @Nullable Slot getOutputSlot(ContainerPlayer handler) {
		return (Slot) handler.inventorySlots.get(0);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree()) {
			if (recipe instanceof EmiCraftingRecipe crafting) {
				return crafting.canFit(2, 2);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ContainerPlayer> context) {
		Container sh = context.getScreenHandler();
		return canFit(sh, recipe) && StandardRecipeHandler.super.canCraft(recipe, context);
	}

	private boolean canFit(Container sh, EmiRecipe recipe) {
		int w = 0;
		int h = 0;
		EmiCraftingRecipe ecr = null;
		if (sh instanceof ContainerWorkbench arsh) {
			w = ((InventoryCraftingAccessor)arsh.craftMatrix).getInventoryWidth();
			h = arsh.craftMatrix.getSizeInventory() / ((InventoryCraftingAccessor)arsh.craftMatrix).getInventoryWidth();
		}
		if (sh instanceof ContainerPlayer arsh) {
			w = ((InventoryCraftingAccessor)arsh.craftMatrix).getInventoryWidth();
			h = arsh.craftMatrix.getSizeInventory() / ((InventoryCraftingAccessor)arsh.craftMatrix).getInventoryWidth();
		}
		if (recipe instanceof EmiCraftingRecipe crafting) {
			ecr = crafting;
		}
		if (ecr != null) {
			return ecr.canFit(w, h);
		}
		return false;
	}

	@Override
	public List<TooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<ContainerPlayer> context) {
		if (!canCraft(recipe, context)) {
			Container sh = context.getScreenHandler();
			if (!canFit(sh, recipe)) {
				return shim.java.List.of(TooltipComponent.of(EmiPort.ordered(TOO_SMALL)));
			}
		}

		return StandardRecipeHandler.super.getTooltip(recipe, context);
	}
}
