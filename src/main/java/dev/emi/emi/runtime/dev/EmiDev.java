package dev.emi.emi.runtime.dev;

import net.minecraft.util.ResourceLocation;

import java.util.Set;

public class EmiDev {
	public static Set<ResourceLocation> duplicateRecipeIds = shim.java.Set.of();
	public static Set<ResourceLocation> incorrectRecipeIds = shim.java.Set.of();
}
