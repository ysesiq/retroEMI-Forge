/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package shim.net.minecraft.client.gui.widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.Drawable;
import shim.net.minecraft.client.gui.Element;
import shim.net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TooltipPositioner;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;

/**
* A clickable widget is a GUI element that has many methods to handle different mouse actions. In
* addition, it allows a message to be rendered on the widget and narrated when the widget is
* selected.
*/
@SideOnly(Side.CLIENT)
public abstract class ClickableWidget extends Gui implements Drawable, Element {
	public enum SelectionType {
		NONE,
        HOVERED,
        FOCUSED
	}

	public static final ResourceLocation WIDGETS_TEXTURE = EmiPort.id("textures/gui/widgets.png");
	public static final ResourceLocation ACCESSIBILITY_TEXTURE = EmiPort.id("textures/gui/accessibility.png");
	protected int width;
	protected int height;
	public int x;
	public int y;
	private Text message;
	protected boolean hovered;
	public boolean active = true;
	public boolean visible = true;
	protected float alpha = 1.0f;
	private int navigationOrder;
	private boolean focused;
	@Nullable
	private List<TooltipComponent> tooltip;
	private int tooltipDelay;
	private long lastHoveredTime;
	private boolean wasHovered;

	private int mouseX, mouseY;

	public ClickableWidget(int x, int y, int width, int height, Text message) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.message = message;
	}

	public int getHeight() {
		return this.height;
	}

	@Override
	public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
		if (!this.visible) {
			return;
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
		this.renderWidget(draw, mouseX, mouseY, delta);
		this.applyTooltip();
	}

	private void applyTooltip() {
		GuiScreen screen;
		boolean bl;
		if (this.tooltip == null) {
			return;
		}
		boolean bl2 = bl = this.hovered || this.isFocused();
		if (bl != this.wasHovered) {
			if (bl) {
				this.lastHoveredTime = Minecraft.getSystemTime();
			}
			this.wasHovered = bl;
		}
		if (bl && Minecraft.getSystemTime() - this.lastHoveredTime > this.tooltipDelay && (screen = Minecraft.getMinecraft().currentScreen) != null) {
			EmiRenderHelper.drawTooltip(screen, EmiDrawContext.instance(), tooltip, mouseX, mouseY, screen.width, getTooltipPositioner());
		}
	}

	protected TooltipPositioner getTooltipPositioner() {
		return HoveredTooltipPositioner.INSTANCE;
	}

	public void setTooltip(@Nullable List<TooltipComponent> tooltip) {
		this.tooltip = tooltip;
	}

	public void setTooltipDelay(int delay) {
		this.tooltipDelay = delay;
	}

	public abstract void renderWidget(DrawContext raw, int mouseX, int mouseY, float tickDelta);

	public void drawTexture(MatrixStack matrices, ResourceLocation texture, int x, int y, int u, int v, int hoveredVOffset, int width, int height, int textureWidth, int textureHeight) {
		int i = v;
		if (!this.isNarratable()) {
			i += hoveredVOffset * 2;
		} else if (this.isSelected()) {
			i += hoveredVOffset;
		}
		GL11.glEnable(GL_DEPTH_TEST);
		EmiDrawContext.instance().drawTexture(texture, x, y, 0, u, i, width, height, textureWidth, textureHeight);
	}

	public void onClick(double mouseX, double mouseY) {
	}

	public void onRelease(double mouseX, double mouseY) {
	}

	protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		boolean bl;
		if (!this.active || !this.visible) {
			return false;
		}
		if (this.isValidClickButton(button) && (bl = this.clicked(mouseX, mouseY))) {
			this.playDownSound();
			this.onClick(mouseX, mouseY);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (this.isValidClickButton(button)) {
			this.onRelease(mouseX, mouseY);
			return true;
		}
		return false;
	}

	protected boolean isValidClickButton(int button) {
		return button == 0;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (this.isValidClickButton(button)) {
			this.onDrag(mouseX, mouseY, deltaX, deltaY);
			return true;
		}
		return false;
	}

	protected boolean clicked(double mouseX, double mouseY) {
		return this.active && this.visible && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.active && this.visible && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
	}

	public void playDownSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(EmiPort.id("gui.button.press"), 1.0F));
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public void setMessage(Text message) {
		this.message = message;
	}

	public Text getMessage() {
		return this.message;
	}

	@Override
	public boolean isFocused() {
		return this.focused;
	}

	public boolean isHovered() {
		return this.hovered;
	}

	public boolean isSelected() {
		return this.isHovered() || this.isFocused();
	}

	public boolean isNarratable() {
		return this.visible && this.active;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public SelectionType getType() {
		if (this.isFocused()) {
			return SelectionType.FOCUSED;
		}
		if (this.hovered) {
			return SelectionType.HOVERED;
		}
		return SelectionType.NONE;
	}

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void forEachChild(Consumer<ClickableWidget> consumer) {
		consumer.accept(this);
	}
}
