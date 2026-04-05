package dev.emi.emi.recipe.special;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

public class EmiBannerShieldRecipe extends EmiPatternCraftingRecipe {
	public static final List<EmiStack> BANNERS = List.of(
		EmiStack.of(Items.BANNER, 1, 0), EmiStack.of(Items.BANNER, 1, 1), EmiStack.of(Items.BANNER, 1, 2), EmiStack.of(Items.BANNER, 1, 3),
		EmiStack.of(Items.BANNER, 1, 4), EmiStack.of(Items.BANNER, 1, 5), EmiStack.of(Items.BANNER, 1, 6), EmiStack.of(Items.BANNER, 1, 7),
		EmiStack.of(Items.BANNER, 1, 8), EmiStack.of(Items.BANNER, 1, 9), EmiStack.of(Items.BANNER, 1, 10), EmiStack.of(Items.BANNER, 1, 11),
		EmiStack.of(Items.BANNER, 1, 12), EmiStack.of(Items.BANNER, 1, 13), EmiStack.of(Items.BANNER, 1, 14), EmiStack.of(Items.BANNER, 1, 15)
	);
	private static final List<EmiStack> EMI_BANNERS = BANNERS.stream().collect(Collectors.toList());
	public static final EmiStack SHIELD = EmiStack.of(Items.SHIELD);

	@SuppressWarnings("unchecked")
	public EmiBannerShieldRecipe(ResourceLocation id) {
		super((List<EmiIngredient>) (List<?>) Stream.concat(Stream.of(SHIELD), EMI_BANNERS.stream()).toList(), EmiStack.of(Items.SHIELD), id);
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new SlotWidget(SHIELD, x, y);
		} else if (slot == 1) {
			return new GeneratedSlotWidget(r -> getPattern(r, null), unique, x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(r -> getPattern(r, Items.SHIELD), unique, x, y);
	}

	public EmiStack getPattern(Random random, Item item) {
		int base = random.nextInt(BANNERS.size());
		if (item == null) {
			item = BANNERS.get(base).getItemStack().getItem();
		}
		ItemStack stack = new ItemStack(item);
		int patterns = 1 + Math.max(random.nextInt(5), random.nextInt(3));
		NBTTagList pattern = new NBTTagList();
		for (int i = 0; i < patterns; i++) {
			pattern = EmiPort.addRandomBanner(pattern, random);
		}

        RetroEMI.setBannerPatterns(stack, pattern);

		return EmiStack.of(stack);
	}
}
