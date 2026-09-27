package shim.net.minecraft.client.gui;

import net.minecraft.client.gui.Gui;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractParentElement
		extends Gui
		implements ParentElement {
	@Nullable
	private Element focused;
	private boolean dragging;

	@Override
	public final boolean isDragging() {
		return this.dragging;
	}

	@Override
	public final void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	@Override
	@Nullable
	public Element getFocused() {
		return this.focused;
	}

	@Override
	public void setFocused(@Nullable Element focused) {
		if (this.focused != null) {
			this.focused.setFocused(false);
		}
		if (focused != null) {
			focused.setFocused(true);
		}
		this.focused = focused;
	}
}
