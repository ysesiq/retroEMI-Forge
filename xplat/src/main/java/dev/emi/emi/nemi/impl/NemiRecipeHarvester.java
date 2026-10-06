package dev.emi.emi.nemi.impl;

import codechicken.nei.recipe.BrewingRecipeHandler;
import codechicken.nei.recipe.FireworkRecipeHandler;
import codechicken.nei.recipe.FuelRecipeHandler;
import codechicken.nei.recipe.FurnaceRecipeHandler;
import codechicken.nei.recipe.RepairRecipeHandler;
import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.ShapelessRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.nemi.NemiCategory;
import dev.emi.emi.nemi.NemiPlugin;
import dev.emi.emi.nemi.NemiRecipe;
import dev.emi.emi.nemi.NemiReflection;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NemiRecipeHarvester {
    // These are handlers that EMI natively covers better (e.g., standard crafting/smelting)
    private static final List<Class<? extends TemplateRecipeHandler>> BLACKLISTED_CLASSES = shim.java.List.of(
        ShapedRecipeHandler.class,
	    ShapelessRecipeHandler.class,
	    FuelRecipeHandler.class,
	    FurnaceRecipeHandler.class,
	    BrewingRecipeHandler.class,
	    RepairRecipeHandler.class,
	    FireworkRecipeHandler.class
    );

    private final EmiRegistry registry;
    private final TemplateRecipeHandler baseHandler;
    private final Map<String, NemiCategory> categories = new HashMap<>();

    public NemiRecipeHarvester(EmiRegistry registry, TemplateRecipeHandler baseHandler) {
        this.registry = registry;
        this.baseHandler = baseHandler;
    }

    public void harvest() {
        if (BLACKLISTED_CLASSES.contains(baseHandler.getClass())) {
            return;
        }

        Set<String> recipeIds = extractRecipeIds();
        if (recipeIds.isEmpty()) {
            return;
        }

        for (String recipeId : recipeIds) {
            harvestRecipesForId(recipeId);
        }
    }

    public Set<String> extractRecipeIds() {
        return recipeTypeIds(baseHandler);
    }

    public static Set<String> recipeTypeIds(TemplateRecipeHandler handler) {
        Set<String> ids = new HashSet<>();

        String overlayId = handler.getOverlayIdentifier();
        if (overlayId != null) {
            ids.add(overlayId);
        }

        try {
            TemplateRecipeHandler tempHandler = handler.newInstance();
            tempHandler.loadTransferRects();

            if (tempHandler.transferRects != null) {
                for (TemplateRecipeHandler.RecipeTransferRect rect : tempHandler.transferRects) {
                    if (rect != null) {
                        String id = NemiReflection.getTransferRectOutputId(rect);
                        if (id != null) {
                            ids.add(id);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return ids;
    }

    private void harvestRecipesForId(String recipeId) {
        TemplateRecipeHandler handler = baseHandler.newInstance();
        try {
            handler.loadCraftingRecipes(recipeId);
        } catch (Exception e) {
            return; // Skip if handler fails to load for this ID
        }

        int numRecipes = handler.numRecipes();
        if (numRecipes <= 0) {
            return;
        }

        NemiCategory category = createCategory(handler, recipeId);
        registerAllRecipes(category, handler, numRecipes, recipeId);
    }

    public NemiCategory createCategory(TemplateRecipeHandler handler, String recipeId) {
        if (categories.containsKey(recipeId)) {
            return categories.get(recipeId);
        }
        NemiCategory category = new NemiCategory(handler, recipeId);
        registry.addCategory(category);
        categories.put(recipeId, category);
        return category;
    }

    public Map<String, NemiCategory> getCategories() {
        return categories;
    }

    private void registerAllRecipes(NemiCategory category, TemplateRecipeHandler handler, int numRecipes, String recipeId) {
        for (int i = 0; i < numRecipes; i++) {
            ResourceLocation emiRecipeId = EmiPort.id(NemiPlugin.DOMAIN, recipeId + "/" + i);
            try {
                NemiRecipe recipe = new NemiRecipe(category, handler, i, emiRecipeId);
                registry.addRecipe(recipe);
            } catch (Throwable t) {
				EmiLog.warn("Skipping unparseable NEI recipe " + emiRecipeId + " (" + handler.getClass().getName() + "): " + t);
            }
        }
    }
}
