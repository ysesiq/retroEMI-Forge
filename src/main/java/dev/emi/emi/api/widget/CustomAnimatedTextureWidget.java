package dev.emi.emi.api.widget;

import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.client.gui.DrawContext;

/**
 * Similar to MCMeta animated
 */
public class CustomAnimatedTextureWidget extends TextureWidget {
	protected final int time, frameCount;
	protected int internalCount = 0;
	protected int oldCount;


	public CustomAnimatedTextureWidget(ResourceLocation texture, int x, int y, int width, int height, int u, int v, int regionWidth, int regionHeight,
                                       int textureWidth, int textureHeight, int time, int frameCount) {
		super(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
		this.time = time;
		this.frameCount = frameCount;
	}

	public CustomAnimatedTextureWidget(ResourceLocation texture, int x, int y, int width, int height, int u, int v, int time, int frameCount) {
		this(texture, x, y, width, height, u, v, width, height, 256, 256, time, frameCount);
	}

	@Override
	public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);

		int totalTime = (int) Minecraft.getMinecraft().theWorld.getTotalWorldTime() % time;
		if (totalTime % time == 0 & totalTime != oldCount || time == 1) {
			internalCount++;
			if (internalCount >= frameCount) {
				internalCount = 0;
			}
		}
		context.drawTexture(this.texture, x, y, width, height, u, (v + (regionHeight * internalCount)), regionWidth, regionHeight, textureWidth, textureHeight);
		this.oldCount = totalTime;
	}

}
