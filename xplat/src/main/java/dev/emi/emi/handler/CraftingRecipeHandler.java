package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.Slot;

public class CraftingRecipeHandler implements StandardRecipeHandler<ContainerWorkbench> {

	@Override
	public List<Slot> getInputSources(ContainerWorkbench handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 10; i++) {
			list.add(handler.getSlot(i));
		}
		int invStart = 10;
		for (int i = invStart; i < invStart + 36; i++) {
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(ContainerWorkbench handler) {
		List<Slot> list = Lists.newArrayList();
		for (int i = 1; i < 10; i++) {
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public @Nullable Slot getOutputSlot(ContainerWorkbench handler) {
		return handler.getSlot(0);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
	}
}
