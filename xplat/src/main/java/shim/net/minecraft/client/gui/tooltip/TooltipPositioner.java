package shim.net.minecraft.client.gui.tooltip;

import shim.net.minecraft.client.util.math.Vec2i;

public interface TooltipPositioner {
	public Vec2i getPosition(int screenWidth, int screenHeight, int x, int y, int w, int h);
}
