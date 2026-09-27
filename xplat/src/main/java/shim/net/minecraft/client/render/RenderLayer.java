package shim.net.minecraft.client.render;

import shim.com.mojang.blaze3d.systems.RenderSystem;

public enum RenderLayer {
	SOLID,
	CUTOUT,
	TRANSLUCENT,
	GLINT;

	public void startDrawing() {
		switch (this) {
			case SOLID, CUTOUT -> RenderSystem.disableBlend();
			case TRANSLUCENT -> {
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
			}
			case GLINT -> RenderSystem.enableBlend();
		}
	}

	public void endDrawing() {
	}
}
