package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.init.Items;
import shim.net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import shim.net.minecraft.util.DyeColor;
import net.minecraft.util.ResourceLocation;

public class EmiFireworkRocketRecipe extends EmiPatternCraftingRecipe {
	private static final List<DyeItem> DYES = Stream.of(DyeColor.values()).map(DyeItem::byColor).collect(Collectors.toList());

	public EmiFireworkRocketRecipe(ResourceLocation id) {
		super(shim.java.List.of(
				EmiStack.of(Items.paper),
						EmiStack.of(Items.firework_charge),
						EmiStack.of(Items.gunpowder)),
				EmiStack.of(Items.fireworks), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(Items.paper), x, y);
		} else {
			final int s = slot - 1;
			return new GeneratedSlotWidget(r -> {
				List<EmiStack> items = getItems(r);
				if (s < items.size()) {
					return items.get(s);
				}
				return EmiStack.EMPTY;
			}, unique, x, y);
		}
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(this::getFireworkRocket, unique, x, y);
	}

	private EmiStack getFireworkRocket(Random random) {
		ItemStack stack = new ItemStack(Items.fireworks);
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagCompound fireworks = new NBTTagCompound();
		NBTTagList explosions = new NBTTagList();

		List<EmiStack> items = getItems(random);
		int gunpowder = 0;
		for (EmiStack item : items) {
			if (item.getId() == EmiStack.of(Items.firework_charge).getId()) {
				explosions.appendTag(item.getNbt().getTagList("Explosion", 0));
			} else if (item.isEqual(EmiStack.of(Items.gunpowder))) {
				gunpowder++;
			}
		}
		if (gunpowder > 1) {
			fireworks.setByte("Flight", (byte) gunpowder);
		}
		if (!(items.isEmpty())) {
			fireworks.setTag("Explosions", explosions);
		}
		tag.setTag("Fireworks", fireworks);
		stack.setTagCompound(tag);
		return EmiStack.of(stack, 3);
	}

	private List<EmiStack> getItems(Random random) {
		List<EmiStack> items = Lists.newArrayList();
		int amount = random.nextInt(3);
		for (int i = 0; i <= amount; i++) {
			items.add(EmiStack.of(Items.gunpowder));
		}
		amount = random.nextInt(8 - items.size());
		for (int i = 0; i <= amount; i++) {
			items.add(getFireworkStar(random));
		}

		return items;
	}

	private List<DyeItem> getDyes(Random random, int max) {
		List<DyeItem> dyes = Lists.newArrayList();
		int amount = 1 + random.nextInt(max);
		for (int i = 0; i < amount; i++) {
			dyes.add(DYES.get(random.nextInt(DYES.size())));
		}
		return dyes;
	}

	private EmiStack getFireworkStar(Random random) {
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

		amount = random.nextInt(2);

		if (amount == 1) {
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
