package dev.emi.emi.mixin.jei;

import java.awt.Rectangle;
import java.util.Set;

import dev.emi.emi.mixin.jei.accessor.GuiIconToggleButtonAccessor;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.widget.SizedButtonWidget;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BookmarkOverlay.class, remap = false)
public class BookmarkOverlayMixin {
	@Shadow @Final private GuiIconToggleButton bookmarkButton;

	@Unique private SizedButtonWidget emiButton = EmiScreenManager.emi;

	@Inject(method = "updateBounds(Ljava/awt/Rectangle;Ljava/util/Set;)V", at = @At("TAIL"))
	private void moveBookmarkButton(Rectangle area, Set<Rectangle> guiExclusionAreas, CallbackInfo ci) {
		((GuiIconToggleButtonAccessor) this.bookmarkButton).getInternalButton().y -= emiButton.visible ? emiButton.getHeight() : 0;
	}
}
