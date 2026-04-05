package dev.emi.emi.screen.widget.config;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import shim.com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import shim.net.minecraft.client.gui.AbstractParentElement;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.Drawable;
import shim.net.minecraft.client.gui.Element;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.gui.widget.ClickableWidget;
import shim.net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

/**
 * Shamelessly modified vanilla lists to support variable width.
 * This is the lesser of two evils, at least this way I have vanilla compat.
 */
public class ListWidget extends AbstractParentElement implements Drawable {
	private static final ResourceLocation MENU_LIST_BACKGROUND_TEXTURE = EmiPort.id("minecraft", "textures/gui/options_background.png");

	protected final Minecraft client;
	private final List<Entry> children = Lists.newArrayList();
	protected int width;
	protected int height;
	protected int top;
	protected int bottom;
	protected int right;
	protected int left;
	private double scrollAmount;
	private boolean renderSelection = true;
	private boolean scrolling;
	private Entry selected;
	private Entry hoveredEntry;
	public int padding = 4;

	public ListWidget(Minecraft client, int width, int height, int top, int bottom) {
		this.client = client;
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
	}

	public void setRenderSelection(boolean renderSelection) {
		this.renderSelection = renderSelection;
	}

	public int getRowWidth() {
		return Math.min(400, width - 60);
	}

	public int getLogicalHeight() {
		return bottom - top;
	}

	@Nullable
	public Entry getSelectedOrNull() {
		return this.selected;
	}

	public void setSelected(@Nullable Entry entry) {
		this.selected = entry;
	}

	public final List<Entry> children() {
		return this.children;
	}

	protected final void clearEntries() {
		this.children.clear();
	}

	protected void replaceEntries(Collection<Entry> newEntries) {
		this.children.clear();
		this.children.addAll(newEntries);
	}

	protected Entry getEntry(int index) {
		return this.children().get(index);
	}

	public int addEntry(Entry entry) {
		this.children.add(entry);
		entry.parentList = this;
		return this.children.size() - 1;
	}

	protected int getEntryCount() {
		return this.children().size();
	}

	protected boolean isSelectedEntry(int index) {
		return Objects.equals(this.getSelectedOrNull(), this.children().get(index));
	}

	@Nullable
	protected final Entry getEntryAtPosition(double x, double y) {
		int rowWidth = this.getRowWidth() / 2;
		int mid = this.left + this.width / 2;
		int rowLeft = mid - rowWidth;
		int rowRight = mid + rowWidth;
		int m = MathHelper.floor(y - (double) this.top) + (int) this.getScrollAmount() - 4;
		if (x < this.getScrollbarPositionX() && x >= rowLeft && x <= rowRight && m >= 0) {
			int h = 0;
			for (int i = 0; i < this.getEntryCount(); i++) {
				int eh = getEntryHeight(i);
				if (m >= h && m < h + eh - padding) {
					return this.getEntry(i);
				}
				h += eh;
			}
		}
		return null;
	}

	public void updateSize(int width, int height, int top, int bottom) {
		this.width = width;
		this.height = height;
		this.top = top;
		this.bottom = bottom;
		this.left = 0;
		this.right = width;
	}

	public void setLeftPos(int left) {
		this.left = left;
		this.right = left + this.width;
	}

	protected int getMaxPosition() {
		return this.getTotalHeight();
	}

	@Override
	public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
		int o;
		int n;
		int m;
		int i = this.getScrollbarPositionX();
		int j = i + 6;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		this.hoveredEntry = this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null;

		{	// Render background
			RenderSystem.enableBlend();
			context.setColor(32.0f / 255.0f, 32.0f / 255.0f, 32.0f / 255.0f, 1.0f);
			ResourceLocation ResourceLocation = MENU_LIST_BACKGROUND_TEXTURE;
			draw.drawTexture(ResourceLocation, left, top, right, bottom + (int)scrollAmount, right - left, bottom - top, 32, 32);
			context.resetColor();
			RenderSystem.disableBlend();
		}

		draw.enableScissor(left, top, right, bottom);
		int k = this.getRowLeft();
		int l = this.top + 4 - (int)this.getScrollAmount();
		this.renderList(draw, k, l, mouseX, mouseY, delta);
		draw.disableScissor();


		{	// Render header & footer separators
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			draw.fillGradient(this.left, this.top, this.right, this.top + 4, 0xFF000000, 0x00000000);
			draw.fillGradient(this.left, this.bottom - 4, this.right, this.bottom, 0x00000000, 0xFF000000);
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		if ((o = this.getMaxScroll()) > 0) {
			GlStateManager.disableTexture2D();
			m = (int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getMaxPosition());
			m = MathHelper.clamp(m, 32, this.bottom - this.top - 8);
			n = (int)this.getScrollAmount() * (this.bottom - this.top - m) / o + this.top;
			if (n < this.top) {
				n = this.top;
			}
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			bufferBuilder.pos(i, this.bottom, 0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(j, this.bottom, 0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(j, this.top, 0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(i, this.top, 0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.pos(i, n + m, 0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.pos(j, n + m, 0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.pos(j, n, 0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.pos(i, n, 0).color(128, 128, 128, 255).endVertex();
			bufferBuilder.pos(i, n + m - 1, 0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.pos(j - 1, n + m - 1, 0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.pos(j - 1, n, 0).color(192, 192, 192, 255).endVertex();
			bufferBuilder.pos(i, n, 0).color(192, 192, 192, 255).endVertex();
			tessellator.draw();
			EmiPort.setPositionTexShader();
		}
		RenderSystem.disableBlend();
	}

	public void centerScrollOn(Entry entry) {
		int i = 0;
		for (Entry e : this.children()) {
			if (e == entry) {
				this.setScrollAmount(i - 42);
				return;
			}
			i += getEntryHeight(e);
		}
	}

	protected void ensureVisible(Entry entry) {
		int i = this.getRowTop(this.children().indexOf(entry));
		int j = i - this.top - 4 - entry.getHeight();
		int k = this.bottom - i - entry.getHeight() * 2;
		if (j < 0) {
			this.scroll(j);
		}
		if (k < 0) {
			this.scroll(-k);
		}
	}

	private void scroll(int amount) {
		this.setScrollAmount(this.getScrollAmount() + (double)amount);
	}

	public double getScrollAmount() {
		return this.scrollAmount;
	}

	public void setScrollAmount(double amount) {
		this.scrollAmount = MathHelper.clamp(amount, 0.0, (double)this.getMaxScroll());
	}

	public int getMaxScroll() {
		return Math.max(0, this.getMaxPosition() - (this.bottom - this.top - 4) + 40);
	}

	protected void updateScrollingState(double mouseX, double mouseY, int button) {
		this.scrolling = button == 0 && mouseX >= (double)this.getScrollbarPositionX() && mouseX < (double)(this.getScrollbarPositionX() + 6);
	}

	protected int getScrollbarPositionX() {
		return this.width - 6;
	}

	public void unfocusTextField() {
		for (Entry e : this.children) {
			for (Element el : e.children()) {
				if (el instanceof TextFieldWidget tfw) {
					EmiPort.focus(tfw, false);
				}
			}
		}
	}

	public TextFieldWidget getFocusedTextField() {
		for (Entry e : this.children) {
			for (Element el : e.children()) {
				if (el instanceof TextFieldWidget tfw) {
					if (tfw.isFocused()) {
						return tfw;
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		/*
		for (Entry entry : this.children()) {
			if (entry.mouseClicked(mouseX, mouseY, button)) {
				this.setFocused((Element)entry);
				this.setDragging(true);
				return true;
			}
		}*/
		unfocusTextField();
		if (!this.isMouseOver(mouseX, mouseY)) {
			return false;
		}
		Entry entry = this.getEntryAtPosition(mouseX, mouseY);
		if (entry != null) {
			if (entry.mouseClicked(mouseX, mouseY, button)) {
				this.setFocused((Element)entry);
				this.setDragging(true);
				return true;
			}
		}
		return this.scrolling;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.getFocused() != null) {
			this.getFocused().mouseReleased(mouseX, mouseY, button);
		}
		return false;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
			return true;
		}
		if (button != 0 || !this.scrolling) {
			return false;
		}
		if (mouseY < (double)this.top) {
			this.setScrollAmount(0.0);
		} else if (mouseY > (double)this.bottom) {
			this.setScrollAmount(this.getMaxScroll());
		} else {
			double d = Math.max(1, this.getMaxScroll());
			int i = this.bottom - this.top;
			int j = MathHelper.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
			double e = Math.max(1.0, d / (double)(i - j));
			this.setScrollAmount(this.getScrollAmount() + deltaY * e);
		}
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		this.setScrollAmount(this.getScrollAmount() - amount * 22);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		/*
		if (keyCode == GLFW.GLFW_KEY_DOWN) {
			this.moveSelection(MoveDirection.DOWN);
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_UP) {
			this.moveSelection(MoveDirection.UP);
			return true;
		}*/
		return false;
	}

	protected void moveSelection(MoveDirection direction) {
		this.moveSelectionIf(direction, entry -> true);
	}

	protected void ensureSelectedEntryVisible() {
		Entry entry = this.getSelectedOrNull();
		if (entry != null) {
			this.setSelected(entry);
			this.ensureVisible(entry);
		}
	}

	/**
	 * Moves the selection in the specified direction until the predicate returns true.
	 *
	 * @param direction the direction to move the selection
	 */
	protected void moveSelectionIf(MoveDirection direction, Predicate<Entry> predicate) {
		int i = direction == MoveDirection.UP ? -1 : 1;
		if (!this.children().isEmpty()) {
			int k;
			int j = this.children().indexOf(this.getSelectedOrNull());
			while (j != (k = MathHelper.clamp(j + i, 0, this.getEntryCount() - 1))) {
				Entry entry = (Entry)this.children().get(k);
				if (predicate.test(entry)) {
					this.setSelected(entry);
					this.ensureVisible(entry);
					break;
				}
				j = k;
			}
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseY >= (double)this.top && mouseY <= (double)this.bottom && mouseX >= (double)this.left && mouseX <= (double)this.right;
	}

	protected void renderList(DrawContext draw, int x, int y, int mouseX, int mouseY, float delta) {
		int i = this.getEntryCount();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		for (int j = 0; j < i; ++j) {
			int p;
			int k = this.getRowTop(j);
			int l = this.getRowBottom(j);
			if (l < this.top || k > this.bottom) continue;
			int m = k;
			int n = getEntryHeight(j);
			if (n == 0) {
				continue;
			}
			n -= 4;
			Entry entry = this.getEntry(j);
			int o = this.getRowWidth();
			if (this.renderSelection && this.isSelectedEntry(j)) {
				p = this.left + this.width / 2 - o / 2;
				int q = this.left + this.width / 2 + o / 2;
				GlStateManager.disableTexture2D();
				float f = this.isFocused() ? 1.0f : 0.5f;
				RenderSystem.setShaderColor(f, f, f, 1.0f);
				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
				bufferBuilder.pos(p, m + n + 2, 0).endVertex();
				bufferBuilder.pos(q, m + n + 2, 0).endVertex();
				bufferBuilder.pos(q, m - 2, 0).endVertex();
				bufferBuilder.pos(p, m - 2, 0).endVertex();
				tessellator.draw();
				RenderSystem.setShaderColor(0.0f, 0.0f, 0.0f, 1.0f);
				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
				bufferBuilder.pos(p + 1, m + n + 1, 0).endVertex();
				bufferBuilder.pos(q - 1, m + n + 1, 0).endVertex();
				bufferBuilder.pos(q - 1, m - 1, 0).endVertex();
				bufferBuilder.pos(p + 1, m - 1, 0).endVertex();
                tessellator.draw();
			}
			p = this.getRowLeft();
			((Entry)entry).render(draw, j, k, p, o - 3, n, mouseX, mouseY, Objects.equals(this.hoveredEntry, entry), delta);
		}
	}

	public int getRowLeft() {
		return this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
	}

	public int getRowRight() {
		return this.getRowLeft() + this.getRowWidth();
	}

	private int getEntryHeight(int i) {
		return getEntryHeight(this.getEntry(i));
	}

	private int getEntryHeight(Entry entry) {
		int h = entry.getHeight();
		if (h == 0) {
			return 0;
		}
		return h + padding;
	}

	protected int getRowTop(int index) {
		int height = 0;
		for (int i = 0; i < index; i++) {
			height += getEntryHeight(i);
		}
		return this.top + 4 - (int)this.getScrollAmount() + height;
	}

	private int getRowBottom(int index) {
		return this.getRowTop(index) + this.getEntry(index).getHeight();
	}

	public boolean isFocused() {
		return false;
	}

	public ClickableWidget.SelectionType getType() {
		if (this.isFocused()) {
			return ClickableWidget.SelectionType.FOCUSED;
		}
		if (this.hoveredEntry != null) {
			return ClickableWidget.SelectionType.HOVERED;
		}
		return ClickableWidget.SelectionType.NONE;
	}

	@Nullable
	public Entry getHoveredEntry() {
		return this.hoveredEntry;
	}

	void setEntryParentList(Entry entry) {
		entry.parentList = this;
	}

	public int getTotalHeight() {
		int height = 0;
		for (int i = 0; i < this.getEntryCount(); i++) {
			height += getEntryHeight(i);
		}
		if (height > 0) {
			height -= padding;
		}
		return height;
	}

	public static abstract class Entry extends AbstractParentElement {
		public ListWidget parentList;

		public abstract void render(DrawContext draw, int index, int y, int x, int width, int height, int mouseX, int mouseY,
			boolean hovered, float delta);

		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return Objects.equals(this.parentList.getEntryAtPosition(mouseX, mouseY), this);
		}

		public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
			return shim.java.List.of();
		}

		public abstract int getHeight();
	}

	protected static enum MoveDirection {
		UP,
		DOWN;

	}
}
