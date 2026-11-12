package dev.emi.emi.mixin.jei.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.ConfigButton;
import mezz.jei.gui.overlay.IngredientListOverlay;

@Mixin(value = IngredientListOverlay.class, remap = false)
public interface IngredientListOverlayAccessor {
	@Accessor("guiScreenHelper") GuiScreenHelper getGuiScreenHelper();
	@Accessor("configButton") ConfigButton getConfigButton();
}
