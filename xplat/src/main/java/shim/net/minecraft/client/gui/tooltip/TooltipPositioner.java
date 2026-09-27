package shim.net.minecraft.client.gui.tooltip;

import net.minecraft.client.gui.GuiScreen;
import shim.net.minecraft.client.util.math.Vec2i;

public interface TooltipPositioner {
	public Vec2i getPosition(GuiScreen screen, int x, int y, int w, int h);
}
