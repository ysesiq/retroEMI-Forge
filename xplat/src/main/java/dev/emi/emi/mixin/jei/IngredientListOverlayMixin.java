package dev.emi.emi.mixin.jei;

import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.SidebarSide;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.input.GuiTextFieldFilter;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IngredientListOverlay.class, remap = false)
public class IngredientListOverlayMixin {
	@Shadow @Final private GuiTextFieldFilter searchField;

	@Inject(method = "updateScreen(Lnet/minecraft/client/gui/GuiScreen;Z)V", at = @At("TAIL"))
	private void nudgeConfigButton(GuiScreen gui, boolean forceUpdate, CallbackInfo ci) {
		if (!EmiConfig.enabled) return;
		if (!EmiConfig.centerSearchBar || !(EmiConfig.searchSidebar == SidebarSide.RIGHT)) {
			JemiUtil.getConfigButton(JemiPlugin.runtime).y = this.searchField.y + 2;
		}
	}
}
