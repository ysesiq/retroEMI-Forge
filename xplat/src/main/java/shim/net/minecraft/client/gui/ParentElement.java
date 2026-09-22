package shim.net.minecraft.client.gui;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A GUI interface which handles keyboard and mouse callbacks for child GUI elements. The
 * implementation of a parent element can decide whether a child element receives keyboard and mouse
 * callbacks.
 */
public interface ParentElement
		extends Element {
	/**
	 * Gets a list of all child GUI elements.
	 */
	public List<? extends Element> children();

	default public Optional<Element> hoveredElement(double mouseX, double mouseY) {
		for (Element element : this.children()) {
			if (!element.isMouseOver(mouseX, mouseY))
				continue;
			return Optional.of(element);
		}
		return Optional.empty();
	}

	@Override
	default public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (Element element : this.children()) {
			if (!element.mouseClicked(mouseX, mouseY, button))
				continue;
			this.setFocused(element);
			if (button == 0) {
				this.setDragging(true);
			}
			return true;
		}
		return false;
	}

	@Override
	default public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.setDragging(false);
		return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseReleased(mouseX, mouseY, button)).isPresent();
	}

	@Override
	default public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.getFocused() != null && this.isDragging() && button == 0) {
			return this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
		return false;
	}

	public boolean isDragging();

	public void setDragging(boolean var1);

	@Override
	default public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, amount)).isPresent();
	}

	@Override
	default public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return this.getFocused() != null && this.getFocused().keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	default public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return this.getFocused() != null && this.getFocused().keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	default public boolean charTyped(char chr, int modifiers) {
		return this.getFocused() != null && this.getFocused().charTyped(chr, modifiers);
	}

	@Nullable
	public Element getFocused();

	public void setFocused(@Nullable Element var1);

	@Override
	default public void setFocused(boolean focused) {
	}

	@Override
	default public boolean isFocused() {
		return this.getFocused() != null;
	}

	default public void focusOn(@Nullable Element element) {
		this.setFocused(element);
	}

}
