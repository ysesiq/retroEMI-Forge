package dev.emi.emi.handler;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.Slot;

public class CookingRecipeHandler<T extends ContainerFurnace> implements StandardRecipeHandler<T> {
	private final EmiRecipeCategory category;

	public CookingRecipeHandler(EmiRecipeCategory category) {
		this.category = category;
	}

	@Override
	public List<Slot> getInputSources(T handler) {
		List<Slot> list = Lists.newArrayList();
		list.add(handler.getSlot(0));
		int invStart = 3;
		for (int i = invStart; i < invStart + 36; i++) {
			list.add(handler.getSlot(i));
		}
		return list;
	}

	@Override
	public List<Slot> getCraftingSlots(T handler) {
		return shim.java.List.of((Slot) handler.inventorySlots.get(0));
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == category && recipe.supportsRecipeTree();
	}
}
