package dev.emi.emi.mixin.nemi;

import codechicken.nei.LayoutManager;
import codechicken.nei.LayoutStyleMinecraft;
import codechicken.nei.VisiblityData;
import codechicken.nei.recipe.GuiRecipe;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LayoutStyleMinecraft.class, remap = false)
public class LayoutStyleMinecraftMixin {
	@Shadow @Final protected static int MARGIN;

	@Unique private SizedButtonWidget emiButton = EmiScreenManager.emi;
	@Unique private SizedButtonWidget treeButton = EmiScreenManager.tree;

	@Inject(method = "layoutFooter", at = @At("TAIL"))
	private void modifyNEIButtonY(GuiContainer gui, VisiblityData visiblity, CallbackInfo ci) {
		if (gui instanceof GuiRecipe) return;
		LayoutManager.options.y -= emiButton.visible ? emiButton.getHeight() + MARGIN : 0;
		LayoutManager.bookmarksButton.y -= treeButton.visible ? treeButton.getHeight() + MARGIN : 0;
	}
}
