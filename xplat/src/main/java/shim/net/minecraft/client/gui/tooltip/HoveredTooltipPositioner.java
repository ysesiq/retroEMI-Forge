package shim.net.minecraft.client.gui.tooltip;

import shim.net.minecraft.client.util.math.Vec2i;

public class HoveredTooltipPositioner implements TooltipPositioner {
	public static final TooltipPositioner INSTANCE = new HoveredTooltipPositioner();

	private HoveredTooltipPositioner() {
	}

	@Override
	public Vec2i getPosition(int screenWidth, int screenHeight, int x, int y, int w, int h) {
		Vec2i v = new Vec2i(x, y).add(12, -12);
		preventOverflow(screenWidth, screenHeight, v, w, h);
		return v;
	}

	private void preventOverflow(int screenWidth, int screenHeight, Vec2i pos, int width, int height) {
		int i;
		if (pos.x + width > screenWidth) {
			pos.x = Math.max(pos.x - 24 - width, 4);
		}
		if (pos.y + (i = height + 3) > screenHeight) {
			pos.y = screenHeight - i;
		}
	}
}
