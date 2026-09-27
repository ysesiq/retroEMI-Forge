package dev.emi.emi.nemi;

import java.lang.reflect.Method;
import java.util.List;

import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.ItemsGrid;
import codechicken.nei.LayoutManager;
import codechicken.nei.LayoutStyleMinecraft;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.RecipeCatalysts;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;

import static dev.emi.emi.nemi.NemiScreenHandler.emiButton;
import static dev.emi.emi.nemi.NemiScreenHandler.treeButton;

public class NemiPlugin implements EmiPlugin {
	public static final String DOMAIN = "nemi";

	public static boolean isNEILoaded = false;

	// NEI buttons are 18x18 pixels, with 1 pixel of spacing (19 pixels total per button step).
	// The block of buttons has a 2-pixel outer margin.
	private static final int NEI_BUTTON_SPACING = 19;
	private static final int NEI_OUTER_MARGIN = 2;

    public static void onLoad() {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			try {
				Class<?> apiClass = Class.forName("codechicken.nei.api.API");
				Method registerMethod = apiClass.getMethod("registerNEIGuiHandler",
					Class.forName("codechicken.nei.api.INEIGuiHandler"));
				Object handler = new NemiScreenHandler();
				registerMethod.invoke(null, handler);

			} catch (Exception e) {
				EmiLog.error("Failed to register NEI GUI handler via reflection", e);
			}
			isNEILoaded = true;
		}
	}

    @Override
    public void register(EmiRegistry registry) {
		if (!(isNEILoaded)) return;
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            registerExclusionArea(registry);

			registerNeiRecipes(registry);
        }
	}

	private void registerExclusionArea(EmiRegistry registry) {
		registry.addGenericExclusionArea((screen, consumer) -> {
			if (!(LayoutManager.getLayoutStyle() instanceof LayoutStyleMinecraft layout)) {
				return;
			}

			if (!(Minecraft.getMinecraft().currentScreen instanceof RecipeScreen)) {
				int rows = (int) Math.ceil((double) layout.buttonCount / layout.numButtons);
				int width = layout.numButtons * NEI_BUTTON_SPACING;
				int height = rows * NEI_BUTTON_SPACING + NEI_OUTER_MARGIN;
				consumer.accept(new Bounds(0, 0, width, height));
				int emiButtonVerticalOffset = emiButton.getHeight() - NEI_OUTER_MARGIN;
				int treeButtonVerticalOffset = treeButton.getHeight() - NEI_OUTER_MARGIN;
				consumer.accept(new Bounds(emiButton.x, emiButton.y - emiButtonVerticalOffset, emiButton.getWidth(), emiButton.getHeight()));
				consumer.accept(new Bounds(treeButton.x, treeButton.y - treeButtonVerticalOffset, treeButton.getWidth(), treeButton.getHeight()));
			}
		});

		registry.addGenericExclusionArea((screen, consumer) -> {
			if (!(screen instanceof GuiContainer) || screen instanceof GuiRecipe<?> || screen instanceof RecipeScreen || LayoutManager.itemPanel == null) {
				return;
			}
			ItemsGrid<?, ?> grid = LayoutManager.itemPanel.getGrid();
			int columns = grid.getColumns();
			for (int r = 0; r < grid.getRows(); r++) {
				for (int c = 0; c < columns; c++) {
					int index = columns * r + c;
					if (grid.isInvalidSlot(index)) {
						Rectangle4i rect = grid.getSlotRect(r, c);
						consumer.accept(new Bounds(rect.x, rect.y, rect.w, rect.h));
					}
				}
			}
		});
	}

	private void registerNeiRecipes(EmiRegistry registry) {
		for (ICraftingHandler baseHandler : GuiCraftingRecipe.craftinghandlers) {
			if (baseHandler instanceof TemplateRecipeHandler templateHandler) {
				NemiRecipeHarvester harvester = new NemiRecipeHarvester(registry, templateHandler);
				harvester.harvest();

				for (NemiRecipeCategory category : harvester.getCategories().values()) {
					List<PositionedStack> catalysts = RecipeCatalysts.getRecipeCatalysts(templateHandler);
					if (catalysts != null) {
						for (PositionedStack stack : catalysts) {
							if (stack != null && stack.item != null) {
								registry.addWorkstation(category, EmiStack.of(stack.item));
							}
						}
					}
				}
			}
		}
	}
}
