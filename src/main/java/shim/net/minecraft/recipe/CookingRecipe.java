package shim.net.minecraft.recipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CookingRecipe {
	private final EmiIngredient input;
	private final ItemStack output;
	private final float experience;

	public CookingRecipe(EmiIngredient input, ItemStack output, float experience) {
		this.input = input;
		this.output = output;
		this.experience = experience;
	}

	public EmiIngredient getIngredients() {
		return input;
	}

	public ItemStack getOutput() {
		return output;
	}

	public float getExperience() {
		return experience;
	}

	public int getCookingTime() {
		return 200;
	}

	public ResourceLocation getId() {
		return EmiPort.id("emi", "/furnace/" + EmiUtil.subId(this.getIngredients().getEmiStacks().getFirst().getItemStack()) + "/" + EmiUtil.subId(this.getOutput()));
	}
}
