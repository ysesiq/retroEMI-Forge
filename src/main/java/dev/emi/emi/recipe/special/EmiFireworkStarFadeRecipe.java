package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.init.Items;
import shim.net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import shim.net.minecraft.util.DyeColor;
import net.minecraft.util.ResourceLocation;

public class EmiFireworkStarFadeRecipe extends EmiPatternCraftingRecipe {
	private static final List<DyeItem> DYES = Stream.of(DyeColor.values()).map(DyeItem::byColor).collect(Collectors.toList());

	public EmiFireworkStarFadeRecipe(ResourceLocation id) {
		super(shim.java.List.of(
			EmiIngredient.of(DYES.stream().map(i -> (EmiIngredient) EmiStack.of(i)).collect(Collectors.toList())),
			EmiStack.of(Items.firework_charge)), EmiStack.of(Items.firework_charge), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		return new GeneratedSlotWidget(r -> {
			EmiStack fireworkStar = getFireworkStar(r, false);
			List<DyeItem> dyeItems = getDyes(r, 8);
			final int s = slot - 1;
			if (slot == 0) {
				return fireworkStar;
			}
			if (s < dyeItems.size()) {
				return EmiStack.of(dyeItems.get(s));
			}
			return EmiStack.EMPTY;
		}, unique, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> getFireworkStar(r, true), unique, x, y);
	}

	private List<DyeItem> getDyes(Random random, int max) {
		List<DyeItem> dyes = Lists.newArrayList();
		int amount = 1 + random.nextInt(max);
		for (int i = 0; i < amount; i++) {
			dyes.add(DYES.get(random.nextInt(DYES.size())));
		}
		return dyes;
	}

	private EmiStack getFireworkStar(Random random, Boolean faded) {
		ItemStack stack = new ItemStack(Items.firework_charge);
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound explosion = new NBTTagCompound();
		int items = 0;

		int amount = random.nextInt(5);

		explosion.setByte("Type", (byte) amount);

		if (!(amount == 0)) {
			items++;
		}

		amount = random.nextInt(4);

		if (amount == 0) {
			explosion.setByte("Flicker", (byte) 1);
			items++;
		} else if (amount == 1) {
			explosion.setByte("Trail", (byte) 1);
			items++;
		} else if (amount == 2) {
			explosion.setByte("Trail", (byte) 1);
			explosion.setByte("Flicker", (byte) 1);
			items = items + 2;
		}

		List<DyeItem> dyeItems = getDyes(random, 8 - items);
		List<Integer> colors = Lists.newArrayList();
		for (DyeItem dyeItem : dyeItems) {
			colors.add(dyeItem.color().getFireworkColor());
		}
		explosion.setIntArray("Colors", colors.stream().mapToInt(Integer::intValue).toArray());

		if (faded) {
			List<DyeItem> dyeItemsFaded = getDyes(random, 8);
			List<Integer> fadedColors = Lists.newArrayList();
			for (DyeItem dyeItem : dyeItemsFaded) {
				fadedColors.add(dyeItem.color().getFireworkColor());
			}
			explosion.setIntArray("FadeColors", fadedColors.stream().mapToInt(Integer::intValue).toArray());
		}

		tag.setTag("Explosion", explosion);
		stack.setTagCompound(tag);
		return EmiStack.of(stack);
	}
}
