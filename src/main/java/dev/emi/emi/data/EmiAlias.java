package dev.emi.emi.data;

import dev.emi.emi.api.stack.EmiIngredient;
import shim.net.minecraft.text.Text;

import java.util.List;

public record EmiAlias(List<EmiIngredient> stacks, List<String> keys) {

	public static record Baked(List<EmiIngredient> stacks, List<Text> text) {
	}
}
