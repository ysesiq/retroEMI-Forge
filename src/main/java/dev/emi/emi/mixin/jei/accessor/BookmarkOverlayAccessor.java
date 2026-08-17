package dev.emi.emi.mixin.jei.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.gui.elements.GuiIconToggleButton;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;

@Mixin(value = BookmarkOverlay.class, remap = false)
public interface BookmarkOverlayAccessor {
	@Accessor("bookmarkButton") GuiIconToggleButton getBookmarkButton();
}
