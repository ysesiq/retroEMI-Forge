package dev.emi.emi.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.mixin.accessor.InventoryCraftingAccessor;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;

public class EmiShapedRecipe extends EmiCraftingRecipe {

	public EmiShapedRecipe(ShapedRecipes recipe) {
		super(padIngredients(recipe), EmiStack.of(EmiPort.getOutput(recipe)), EmiPort.getId(recipe), false);
		setRemainders(input, recipe);
	}

	public static void setRemainders(List<EmiIngredient> input, IRecipe recipe) {
		try {
            InventoryCrafting inv = EmiUtil.getCraftingInventory();
			for (int i = 0; i < input.size(); i++) {
				if (input.get(i).isEmpty()) {
					continue;
				}
				for (int j = 0; j < input.size(); j++) {
					if (j == i) {
						continue;
					}
					if (!input.get(j).isEmpty()) {
						inv.setInventorySlotContents(j, input.get(j).getEmiStacks().get(0).getItemStack().copy());
					}
				}
				List<EmiStack> stacks = input.get(i).getEmiStacks();
				for (EmiStack stack : stacks) {
					inv.setInventorySlotContents(i, stack.getItemStack().copy());
                    if (stack.getItemStack().getItem().hasContainerItem()) {
                        stack.setRemainder(EmiStack.of(stack.getItemStack().getItem().getContainerItem()));
					}
				}
                Arrays.fill(((InventoryCraftingAccessor) inv).getStackList(), null);
			}
		} catch (Exception e) {
			EmiLog.error("Exception thrown setting remainders for " + EmiPort.getId(recipe), e);
		}
	}

	private static List<EmiIngredient> padIngredients(ShapedRecipes recipe) {
		List<EmiIngredient> list = Lists.newArrayList();
		int i = 0;
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 3; x++) {
				if (x >= recipe.recipeWidth || y >= recipe.recipeHeight || i >= Arrays.stream(recipe.recipeItems).collect(Collectors.toList()).size()) {
					list.add(EmiStack.EMPTY);
				} else {
					list.add(EmiStack.of((Arrays.stream(recipe.recipeItems).collect(Collectors.toList()).get(i++))));
				}
			}
		}
		return list;
	}
}
