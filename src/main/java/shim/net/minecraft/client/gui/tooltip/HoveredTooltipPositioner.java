package shim.net.minecraft.client.gui.tooltip;

import net.minecraft.client.gui.GuiScreen;
import shim.net.minecraft.client.util.math.Vec2i;

public class HoveredTooltipPositioner implements TooltipPositioner {
	public static final TooltipPositioner INSTANCE = new HoveredTooltipPositioner();

	private HoveredTooltipPositioner() {
	}

	@Override
	public Vec2i getPosition(GuiScreen screen, int x, int y, int w, int h) {
		Vec2i v = new Vec2i(x+12, y-12);
		preventOverflow(screen, v, w, h);
		return v;
	}

	private void preventOverflow(GuiScreen screen, Vec2i pos, int width, int height) {
		int i;
		if (pos.x + width > screen.width) {
			pos.x = Math.max(pos.x - 24 - width, 4);
		}
		if (pos.y + (i = height + 3) > screen.height) {
			pos.y = screen.height - i;
		}
	}
}
