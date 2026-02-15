package dev.emi.emi.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.screen.BoMScreen;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeManager;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.recipe.EmiSyntheticIngredientRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiFavorite;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;

public class EmiApi {
	private static final Minecraft client = Minecraft.getMinecraft();

	public static List<EmiStack> getIndexStacks() {
		return EmiStackList.stacks;
	}

	public static EmiRecipeManager getRecipeManager() {
		return EmiRecipes.manager;
	}

	public static boolean isCheatMode() {
        return switch (EmiConfig.cheatMode) {
            case TRUE -> true;
            case CREATIVE -> client.thePlayer == null || client.thePlayer.capabilities.isCreativeMode;
            case FALSE -> false;
        };
    }

	/**
	 * @return Current search text
	 */
	public static String getSearchText() {
		return EmiScreenManager.search.getText();
	}

	/**
	 * Sets the current search to the provided query
	 */
	public static void setSearchText(String text) {
		EmiScreenManager.search.setText(text);
	}

	public static boolean isSearchFocused() {
		return EmiScreenManager.search.isFocused();
	}

	/**
	 * Gets the currently hovered EmiIngredient at the provided screen coordinates,
	 * or {@link EmiStack#EMPTY} if none.
	 * @param includeStandard Whether to include the EmiIngredient representation of
	 * 	standard stacks in slots or otherwise provided to EMI.
	 */
	public static EmiStackInteraction getHoveredStack(int mouseX, int mouseY, boolean includeStandard) {
		return EmiScreenManager.getHoveredStack(mouseX, mouseY, includeStandard);
	}

	/**
	 * Gets the currently hovered EmiIngredient at the mouse or {@link EmiStack#EMPTY} if none.
	 * @param includeStandard Whether to include the EmiIngredient representation of
	 * 	standard stacks in slots or otherwise provided to EMI.
	 */
	public static EmiStackInteraction getHoveredStack(boolean includeStandard) {
		return EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, includeStandard);
	}

	/**
	 * @return Recipe context associated with specific ingredient implementations.
	 * This could be favorites, craftables, or something else.
	 */
	public static @Nullable EmiRecipe getRecipeContext(EmiIngredient stack) {
		if (stack instanceof EmiFavorite fav) {
			return fav.getRecipe();
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	public static GuiContainer getHandledScreen() {
		GuiScreen s = client.currentScreen;
		if (s instanceof GuiContainer container) {
			return container;
		} else if (s instanceof RecipeScreen rs) {
			return rs.old;
		} else if (s instanceof BoMScreen bs) {
			return bs.old;
		}
		return null;
	}

    @SideOnly(Side.CLIENT)
	public static void displayAllRecipes() {
		EmiRecipeManager manager = EmiApi.getRecipeManager();
		setPages(manager.getCategories().stream().collect(Collectors.toMap(c -> c, c -> manager.getRecipes(c))), EmiStack.EMPTY);
	}

    @SideOnly(Side.CLIENT)
	public static void displayRecipeCategory(EmiRecipeCategory category) {
		setPages(Collections.singletonMap(category, getRecipeManager().getRecipes(category)), EmiStack.EMPTY);
	}

    @SideOnly(Side.CLIENT)
	public static void displayRecipe(EmiRecipe recipe) {
		setPages(Collections.singletonMap(recipe.getCategory(), Collections.singletonList(recipe)), EmiStack.EMPTY);
	}

    @SideOnly(Side.CLIENT)
	public static void displayRecipes(EmiIngredient stack) {
		if (stack instanceof EmiFavorite fav) {
			stack = fav.getStack();
		}
		if (stack instanceof TagEmiIngredient tag) {
			for (EmiRecipe recipe : getRecipeManager().getRecipes(VanillaPlugin.TAG)) {
				if (recipe instanceof EmiTagRecipe tr && tr.key.equals(tag.key)) {
					setPages(Collections.singletonMap(VanillaPlugin.TAG, shim.java.List.of(recipe)), stack);
					break;
				}
			}
		} else if (stack instanceof ListEmiIngredient list) {
			setPages(Collections.singletonMap(VanillaPlugin.INGREDIENT, Collections.singletonList(new EmiSyntheticIngredientRecipe(stack))), stack);
		} else if (stack.getEmiStacks().size() == 1) {
			EmiStack es = stack.getEmiStacks().get(0);
			setPages(mapRecipes(pruneSources(EmiApi.getRecipeManager().getRecipesByOutput(es), es)), stack);
			focusRecipe(BoM.getRecipe(es));
		}
	}

    @SideOnly(Side.CLIENT)
	public static void displayUses(EmiIngredient stack) {
		if (!stack.isEmpty()) {
			EmiStack zero = stack.getEmiStacks().get(0);
			Map<EmiRecipeCategory, List<EmiRecipe>> map
				= mapRecipes(Stream.concat(
						pruneUses(getRecipeManager().getRecipesByInput(zero), stack).stream(),
						EmiRecipes.byWorkstation.getOrDefault(zero, Collections.emptyList()).stream()).distinct().collect(Collectors.toList()));
			setPages(map, stack);
		}
	}

    @SideOnly(Side.CLIENT)
	public static void viewRecipeTree() {
		if (client.currentScreen == null) {
			//noinspection RedundantCast
			client.displayGuiScreen(new GuiInventory((EntityPlayer) (Object) client.thePlayer));
		}
		GuiScreen s = client.currentScreen;
		if (s instanceof GuiContainer hs) {
			push();
			client.displayGuiScreen(new BoMScreen(hs));
		} else if (s instanceof RecipeScreen rs) {
			push();
			client.displayGuiScreen(new BoMScreen(rs.old));
		}
	}

    @SideOnly(Side.CLIENT)
	public static void focusRecipe(EmiRecipe recipe) {
		if (client.currentScreen instanceof RecipeScreen rs) {
			rs.focusRecipe(recipe);
		}
	}

    @SideOnly(Side.CLIENT)
	private static void push() {
		if (client.currentScreen instanceof RecipeScreen rs) {
			EmiHistory.push(rs);
		} else if (client.currentScreen instanceof BoMScreen bs) {
			EmiHistory.push(bs);
		} else {
			EmiHistory.clear();
			EmiHistory.push(client.currentScreen);
		}
	}

	private static List<EmiRecipe> pruneSources(List<EmiRecipe> list, EmiStack context) {
		return list.stream().filter(r -> {
			return r.getOutputs().stream().anyMatch(i -> i.isEqual(context));
		}).collect(Collectors.toList());
	}

	private static List<EmiRecipe> pruneUses(List<EmiRecipe> list, EmiIngredient context) {
		return list.stream().filter(r -> {
			return r.getInputs().stream().anyMatch(i -> containsAll(i, context))
				|| r.getCatalysts().stream().anyMatch(i -> containsAll(i, context));
		}).sorted((a, b) -> getSmallestPresence(a, context) - getSmallestPresence(b, context)).collect(Collectors.toList());
	}

	private static int getSmallestPresence(EmiRecipe recipe, EmiIngredient context) {
		int ideal = context.getEmiStacks().size();
		int smallestPresence = Integer.MAX_VALUE;
		for (EmiIngredient i : recipe.getInputs()) {
			if (containsAll(i, context)) {
				smallestPresence = Math.min(smallestPresence, i.getEmiStacks().size());
				if (smallestPresence <= ideal) {
					break;
				}
			}
		}
		return smallestPresence;
	}

	private static Map<EmiRecipeCategory, List<EmiRecipe>> mapRecipes(List<EmiRecipe> list) {
		Map<EmiRecipeCategory, List<EmiRecipe>> map = Maps.newHashMap();
		for (EmiRecipe recipe : list) {
			map.computeIfAbsent(recipe.getCategory(), k -> Lists.newArrayList()).add(recipe);
		}
		return map;
	}

	private static boolean containsAll(EmiIngredient collection, EmiIngredient ingredient) {
		outer:
		for (EmiStack ing : ingredient.getEmiStacks()) {
			for (EmiStack col : collection.getEmiStacks()) {
				if (col.isEqual(ing)) {
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

    @SideOnly(Side.CLIENT)
	private static void setPages(Map<EmiRecipeCategory, List<EmiRecipe>> recipes, EmiIngredient stack) {
		recipes = recipes.entrySet().stream().filter(e -> !e.getValue().isEmpty())
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		if (!recipes.isEmpty()) {
			EmiSidebars.lookup(stack);
			if (getHandledScreen() == null) {
				//noinspection RedundantCast
				client.displayGuiScreen(new GuiInventory((EntityPlayer) (Object) client.thePlayer));
			}
			GuiScreen s = client.currentScreen;
			if (s instanceof GuiContainer hs) {
				push();
				client.displayGuiScreen(new RecipeScreen(hs, recipes));
			} else if (s instanceof BoMScreen bs) {
				push();
				client.displayGuiScreen(new RecipeScreen(bs.old, recipes));
			} else if (s instanceof RecipeScreen rs) {
				push();
				RecipeScreen n = new RecipeScreen(rs.old, recipes);
				client.displayGuiScreen(n);
				n.focusCategory(rs.getFocusedCategory());
			}
		}
	}
}
