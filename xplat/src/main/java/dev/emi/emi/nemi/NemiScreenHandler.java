package dev.emi.emi.nemi;

import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.ButtonCycled;
import codechicken.nei.LayoutManager;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiAdapter;
import codechicken.nei.recipe.GuiRecipe;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.gui.inventory.GuiContainer;

public class NemiScreenHandler extends INEIGuiAdapter {
	static SizedButtonWidget emiButton = EmiScreenManager.emi;
	static SizedButtonWidget treeButton = EmiScreenManager.tree;

	@Override
	public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {

		if (EmiConfig.enabled)
			currentVisibility.showItemSection =
				currentVisibility.enableDeleteMode =
					currentVisibility.showSearchSection = false;

		return currentVisibility;
	}

	@Override
	public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
		if (gui instanceof GuiRecipe<?> || !emiButton.visible) return false;

		ButtonCycled options = LayoutManager.options;
		ButtonCycled bookmarksButton = LayoutManager.bookmarksButton;

		Rectangle4i bounds = new Rectangle4i(x, y, w, h);
		Rectangle4i emiBtnBounds = new Rectangle4i(emiButton.x, emiButton.y, emiButton.getWidth(), emiButton.getHeight());
		Rectangle4i treeBounds = new Rectangle4i(treeButton.x, treeButton.y, treeButton.getWidth(), treeButton.getHeight());
		Rectangle4i optionBounds = new Rectangle4i(options.x, options.y, 20, 20);
		Rectangle4i bmBounds = new Rectangle4i(bookmarksButton.x, bookmarksButton.y, 20, 20);

		return bounds.intersects(emiBtnBounds) || bounds.intersects(treeBounds)
			|| bounds.intersects(optionBounds) || bounds.intersects(bmBounds);
	}
}
