package dev.emi.emi.runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class ProxyRecipeManager {
	private static final Minecraft client = Minecraft.getMinecraft();
	private static Map<IRecipe, ResourceLocation> recipeIds = shim.java.Map.of();

	public static boolean isAvailable() {
		return getRaw() != null;
	}

	public static IForgeRegistry<IRecipe> getRaw() {
		World world = client.world;
		if (world == null) {
			return null;
		}
		return ForgeRegistries.RECIPES;
	}

	public static ResourceLocation getId(IRecipe recipe) {
		return recipeIds.get(recipe);
	}

	public static boolean hasId(ResourceLocation id) {
		return recipeIds.containsValue(id);
	}

	public static @Nullable IRecipe getRecipe(ResourceLocation id) {
		IRecipe recipe = getRecipeEntry(id);
		if (recipe == null) {
			return null;
		}
		return recipe;
	}

	public static @Nullable IRecipe getRecipeEntry(ResourceLocation id) {
		IForgeRegistry<IRecipe> raw = getRaw();
		if (raw == null || id == null) {
			return null;
		}
		return raw.getValue(id);
	}

	public static Stream<IRecipe> streamMatches(InventoryCrafting inventory) {
		IForgeRegistry<IRecipe> raw = getRaw();
		if (raw == null) {
			return Stream.empty();
		}
		return raw.getValues().stream().filter(recipe -> recipe.matches(inventory, client.world));
	}

	public static List<IRecipe> getMatches(InventoryCrafting inventory) {
		return streamMatches(inventory).collect(Collectors.toList());
	}

	public static @Nullable IRecipe getFirst(InventoryCrafting inventory) {
		IForgeRegistry<IRecipe> raw = getRaw();
		if (raw == null) {
			return null;
		}
		return raw.getValues().stream().filter(recipe -> recipe.matches(inventory, client.world)).findFirst().orElse(null);
	}

	public static void bakeIds() {
		IForgeRegistry<IRecipe> raw = getRaw();
		if (raw == null) {
			return;
		}
		recipeIds = new Reference2ObjectOpenHashMap<>();
		for (IRecipe recipe : raw.getValues()) {
			recipeIds.put(recipe, raw.getKey(recipe));
		}
	}
}
