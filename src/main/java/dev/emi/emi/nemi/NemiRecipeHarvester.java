package dev.emi.emi.nemi;

import codechicken.nei.recipe.BrewingRecipeHandler;
import codechicken.nei.recipe.FireworkRecipeHandler;
import codechicken.nei.recipe.FuelRecipeHandler;
import codechicken.nei.recipe.FurnaceRecipeHandler;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.RepairRecipeHandler;
import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.ShapelessRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
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

    private static final EmiStack DEFAULT_ICON = EmiStack.of(Blocks.crafting_table);

    private final EmiRegistry registry;
    private final TemplateRecipeHandler baseHandler;
    private final Map<String, NemiRecipeCategory> categories = new HashMap<>();

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
                // TODO is there a cleaner way to do this than reflection?
                Field outputIdField = TemplateRecipeHandler.RecipeTransferRect.class.getDeclaredField("outputId");
                outputIdField.setAccessible(true);

                for (TemplateRecipeHandler.RecipeTransferRect rect : tempHandler.transferRects) {
                    if (rect != null) {
                        String id = (String) outputIdField.get(rect);
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

        NemiRecipeCategory category = createCategory(handler, recipeId);
        registerAllRecipes(category, handler, numRecipes, recipeId);
    }

    public NemiRecipeCategory createCategory(TemplateRecipeHandler handler, String recipeId) {
        if (categories.containsKey(recipeId)) {
            return categories.get(recipeId);
        }
        ResourceLocation categoryId = EmiPort.id(getModId(handler), recipeId);
        EmiRenderable icon = determineCategoryIcon(handler);

        NemiRecipeCategory category = new NemiRecipeCategory(categoryId, icon, handler.getRecipeName());
        registry.addCategory(category);
        categories.put(recipeId, category);
        return category;
    }

    public Map<String, NemiRecipeCategory> getCategories() {
        return categories;
    }

    private void registerAllRecipes(NemiRecipeCategory category, TemplateRecipeHandler handler, int numRecipes, String recipeId) {
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

    private EmiRenderable determineCategoryIcon(TemplateRecipeHandler handler) {
	    HandlerInfo info = GuiRecipeTab.getHandlerInfo(handler);
	    if (info == null) return DEFAULT_ICON;
	    if (info.getItemStack() != null) {
		    return EmiStack.of(info.getItemStack());
	    } else if (info.getImage() != null) {
		    return new NemiRenderable(info.getImage());
	    }
		return DEFAULT_ICON;
    }

    private static String getModId(TemplateRecipeHandler handler) {
        HandlerInfo info = GuiRecipeTab.getHandlerInfo(handler);
		if (info == null || info.getModId() == null) return NemiPlugin.DOMAIN;
        return info.getModId();
    }
}
