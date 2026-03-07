package dev.emi.emi.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.github.bsideup.jabel.Desugar;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import cpw.mods.fml.common.FMLCommonHandler;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class RecipeDefaults {
	public final List<ResourceLocation> added = Lists.newArrayList();
	public final List<Resolution> resolutions = Lists.newArrayList();
	public final List<Tag> tags = Lists.newArrayList();

	public void add(ResourceLocation id) {
		added.add(id);
	}

	public void add(ResourceLocation id, JsonArray arr) {
		List array = new ArrayList<>(Collections.singleton(arr));
		resolutions.add(new Resolution(id, array));
	}

	public void addTag(JsonElement tag, JsonElement stack) {
		tags.add(new Tag(tag, stack));
	}

	public void remove(ResourceLocation id) {
		added.remove(id);
		resolutions.removeIf(r -> r.recipe.equals(id));
	}

	public void clear() {
		added.clear();
		resolutions.clear();
		tags.clear();
	}

	public Map<EmiIngredient, EmiRecipe> bake() {
		Map<EmiIngredient, EmiRecipe> map = Maps.newHashMap();
		if (!FMLCommonHandler.instance().getSide().isServer()) {
			Minecraft client = Minecraft.getMinecraft();
			if (client.theWorld == null) {
				return map;
			}
		}
		EmiRecipeManager manager = EmiApi.getRecipeManager();
		for (ResourceLocation id : added) {
			EmiRecipe recipe = manager.getRecipe(id);
			if (recipe != null) {
				for (EmiIngredient stack : recipe.getOutputs()) {
					map.put(stack, recipe);
				}
			}
		}
		for (Resolution r : resolutions) {
			EmiRecipe recipe = manager.getRecipe(r.recipe);
			if (recipe != null) {
				for (JsonElement el : r.stacks) {
					EmiIngredient stack = EmiIngredientSerializer.getDeserialized(el);
					if (!stack.isEmpty()) {
						map.put(stack, recipe);
					}
				}
			}
		}
		for (Tag t : tags) {
			EmiIngredient tag = EmiIngredientSerializer.getDeserialized(t.tag);
			EmiIngredient stack = EmiIngredientSerializer.getDeserialized(t.stack);
			if (!tag.isEmpty() && !stack.isEmpty() && stack.getEmiStacks().size() == 1 && new HashSet<>(tag.getEmiStacks()).containsAll(stack.getEmiStacks())) {
				map.put(tag, new EmiResolutionRecipe(tag, stack.getEmiStacks().get(0)));
			}
		}
		return map;
	}


	@Desugar
	public static record Resolution(ResourceLocation recipe, List<JsonElement> stacks) {
	}


	@Desugar
	public static record Tag(JsonElement tag, JsonElement stack) {
	}
}
