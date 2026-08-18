package dev.emi.emi.nemi;

import codechicken.nei.drawable.DrawableResource;
import dev.emi.emi.api.render.EmiRenderable;
import shim.net.minecraft.client.gui.DrawContext;

public class NemiRenderable implements EmiRenderable {
	private DrawableResource image;

	public NemiRenderable(DrawableResource image) {
		this.image = image;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta) {
		image.draw(x, y);
	}
}
