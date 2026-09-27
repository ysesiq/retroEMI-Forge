package dev.emi.emi.runtime;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import shim.net.minecraft.util.SyntheticIdentifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProxyRecipeManager {
	private static final Minecraft client = Minecraft.getMinecraft();
	private static Map<IRecipe, ResourceLocation> recipeIds = shim.java.Map.of();

	public static boolean isAvailable() {
		return getRaw() != null;
	}

	public static CraftingManager getRaw() {
		World world = client.theWorld;
		if (world == null) {
			return null;
		}
		return CraftingManager.getInstance();
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
		CraftingManager raw = getRaw();
		if (raw == null || id == null) {
			return null;
		}
		for (Map.Entry<IRecipe, ResourceLocation> entry : recipeIds.entrySet()) {
			if (id.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static Stream<IRecipe> streamMatches(InventoryCrafting inventory) {
		CraftingManager raw = getRaw();
		if (raw == null) {
			return Stream.empty();
		}
		return ((List<IRecipe>) raw.getRecipeList()).stream().filter(recipe -> recipe.matches(inventory, client.theWorld));
	}

	public static List<IRecipe> getMatches(InventoryCrafting inventory) {
		return streamMatches(inventory).collect(Collectors.toList());
	}

	public static @Nullable IRecipe getFirst(InventoryCrafting inventory) {
		CraftingManager raw = getRaw();
		if (raw == null) {
			return null;
		}
		return ((List<IRecipe>) raw.getRecipeList()).stream().filter(recipe -> recipe.matches(inventory, client.theWorld)).findFirst().orElse(null);
	}

	public static void bakeIds() {
		CraftingManager raw = getRaw();
		if (raw == null) {
			return;
		}
		recipeIds = new Reference2ObjectOpenHashMap<>();
		for (IRecipe recipe : (List<IRecipe>) raw.getRecipeList()) {
			recipeIds.put(recipe, SyntheticIdentifier.generateId(recipe));
		}
	}
}
