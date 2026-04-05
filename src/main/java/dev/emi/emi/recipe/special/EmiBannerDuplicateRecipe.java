package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class EmiBannerDuplicateRecipe extends EmiPatternCraftingRecipe {

	public static final List<ItemStack> BANNERS = List.of(
			new ItemStack(Items.BANNER, 1, 0), new ItemStack(Items.BANNER, 1, 1), new ItemStack(Items.BANNER, 1, 2), new ItemStack(Items.BANNER, 1, 3),
			new ItemStack(Items.BANNER, 1, 4), new ItemStack(Items.BANNER, 1, 5), new ItemStack(Items.BANNER, 1, 6), new ItemStack(Items.BANNER, 1, 7),
			new ItemStack(Items.BANNER, 1, 8), new ItemStack(Items.BANNER, 1, 9), new ItemStack(Items.BANNER, 1, 10), new ItemStack(Items.BANNER, 1, 11),
			new ItemStack(Items.BANNER, 1, 12), new ItemStack(Items.BANNER, 1, 13), new ItemStack(Items.BANNER, 1, 14), new ItemStack(Items.BANNER, 1, 15)
	);

	private final ItemStack banner;

	public EmiBannerDuplicateRecipe(ItemStack banner, ResourceLocation id) {
		super(List.of(
				EmiStack.of(banner),
				EmiStack.of(banner).setRemainder(EmiStack.of(banner))),
				EmiStack.of(banner), id);
		this.banner = banner;
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(EmiStack.of(banner), x, y);
		} else if (slot == 1) {
			return new GeneratedSlotWidget(r -> getPattern(r, true), unique, x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> getPattern(r, false) , unique, x, y);
	}

	public EmiStack getPattern(Random random, boolean reminder) {
		int patterns = 1 + Math.max(random.nextInt(5), random.nextInt(3));
		NBTTagList pattern = new NBTTagList();
		for (int i = 0; i < patterns; i++) {
			pattern = EmiPort.addRandomBanner(pattern, random);
		}

		RetroEMI.setBannerPatterns(banner, pattern);

		EmiStack emiStack = EmiStack.of(banner);
		if (reminder) {
			emiStack.setRemainder(EmiStack.of(banner));
		}
		return emiStack;
	}
}
