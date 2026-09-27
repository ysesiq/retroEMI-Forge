package dev.emi.emi.recipe.special;

import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import shim.java.List;

import java.util.Random;

public class EmiMapCloningRecipe extends EmiPatternCraftingRecipe {

	public EmiMapCloningRecipe(ResourceLocation id) {
		super(List.of(
						EmiStack.of(Items.filled_map),
						EmiStack.of(Items.map)),
				EmiStack.of(Items.filled_map), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(Items.filled_map), x, y);
		} else {
			final int s = slot - 1;
			return new GeneratedSlotWidget(r -> {
				int amount = r.nextInt(8) + 1;
				if (s < amount) {
					return EmiStack.of(Items.map);
				}
				return EmiStack.EMPTY;
			}, unique, x, y);
		}
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> EmiStack.of(Items.filled_map, r.nextInt(8) + 2), unique, x, y);
	}

	public EmiStack getAmount(Random random, Item item) {
		return EmiStack.of(item);
	}
}
