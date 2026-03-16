package shim.net.minecraft.client.gui;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import com.rewindmc.retroemi.RetroEMI;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.text.OrderedText;
import shim.net.minecraft.text.Text;
import shim.net.minecraft.util.math.ColorHelper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DrawContext extends Gui {
	private final Minecraft client;
	private final MatrixStack matrices;
    private final TextureMap guiAtlasManager;
    public static final DrawContext INSTANCE = new DrawContext(Minecraft.getMinecraft(), MatrixStack.INSTANCE);

	public DrawContext(Minecraft client, MatrixStack matrices) {
		this.client = client;
		this.matrices = matrices;
        this.guiAtlasManager = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE));
	}

	public MatrixStack getMatrices() {
		return matrices;
	}

	public void enableScissor(int x1, int y1, int x2, int y2) {
		ScaledResolution scaledResolution = new ScaledResolution(client);
		int scale = scaledResolution.getScaleFactor();
		int x = Math.min(x1, x2) * scale;
		int y = client.displayHeight - Math.max(y1, y2) * scale;
		int width = Math.abs(x2 - x1) * scale;
		int height = Math.abs(y2 - y1) * scale;
		RenderSystem.enableScissor(x, y, width, height);
	}

	public void disableScissor() {
		RenderSystem.disableScissor();
	}

	public void setShaderColor(float red, float green, float blue, float alpha) {
		RenderSystem.setShaderColor(red, green, blue, alpha);
	}

	public void fill(int x1, int y1, int x2, int y2, int color) {
		this.fill(x1, y1, x2, y2, 0, color);
	}

	public void fill(int x1, int y1, int x2, int y2, int z, int color) {
		int i;

		if (x1 < x2) {
			i = x1;
			x1 = x2;
			x2 = i;
		}

		if (y1 < y2) {
			i = y1;
			y1 = y2;
			y2 = i;
		}

		float r = (float)(color >> 24 & 255) / 255.0F;
		float g = (float)(color >> 16 & 255) / 255.0F;
		float b = (float)(color >> 8 & 255) / 255.0F;
		float a = (float)(color & 255) / 255.0F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		RenderSystem.enableBlend();
        GlStateManager.disableTexture2D();
		RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(x1, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.pos(x2, y2, z).color(r, g, b, a).endVertex();
        bufferBuilder.pos(x2, y1, z).color(r, g, b, a).endVertex();
        bufferBuilder.pos(x1, y1, z).color(r, g, b, a).endVertex();
        EmiPort.draw(bufferBuilder);
        EmiPort.setPositionTexShader();
		RenderSystem.disableBlend();
	}

	public void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
		this.fillGradient(startX, startY, endX, endY, 0, colorStart, colorEnd);
	}

	public void fillGradient(int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
		float rs = (float)(colorStart >> 24 & 255) / 255.0F;
		float gs = (float)(colorStart >> 16 & 255) / 255.0F;
		float bs = (float)(colorStart >> 8 & 255) / 255.0F;
		float as = (float)(colorStart & 255) / 255.0F;
		float re = (float)(colorEnd >> 24 & 255) / 255.0F;
		float ge = (float)(colorEnd >> 16 & 255) / 255.0F;
		float be = (float)(colorEnd >> 8 & 255) / 255.0F;
		float ae = (float)(colorEnd & 255) / 255.0F;
        GlStateManager.disableTexture2D();
		RenderSystem.enableBlend();
        GlStateManager.disableAlpha();
		RenderSystem.defaultBlendFunc();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(endX, startY, z).color(gs, bs, as, rs).endVertex();
		bufferBuilder.pos(startX, startY, z).color(gs, bs, as, rs).endVertex();
		bufferBuilder.pos(startX, endY, z).color(ge, be, ae, re).endVertex();
		bufferBuilder.pos(endX, endY, z).color(ge, be, ae, re).endVertex();
		EmiPort.draw(bufferBuilder);
        GlStateManager.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		GlStateManager.enableAlpha();
        EmiPort.setPositionTexShader();
	}

	public void drawCenteredTextWithShadow(FontRenderer textRenderer, String text, int centerX, int y, int color) {
		this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getStringWidth(text) / 2, y, color);
	}

	public void drawCenteredTextWithShadow(FontRenderer textRenderer, Text text, int centerX, int y, int color) {
		OrderedText orderedText = text.asOrderedText();
		this.drawTextWithShadow(textRenderer, orderedText, centerX - textRenderer.getStringWidth(orderedText.asString()) / 2, y, color);
	}

	public void drawCenteredTextWithShadow(FontRenderer textRenderer, OrderedText text, int centerX, int y, int color) {
		this.drawTextWithShadow(textRenderer, text, centerX - textRenderer.getStringWidth(text.asString()) / 2, y, color);
	}

	public int drawTextWithShadow(FontRenderer textRenderer, @Nullable String text, int x, int y, int color) {
		return this.drawText(textRenderer, text, x, y, color, true);
	}

	public int drawText(FontRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow) {
		if (text == null) {
			return 0;
		} else {
			int i = textRenderer.drawString(text, x, y, color, shadow);
			return i;
		}
	}

	public int drawTextWithShadow(FontRenderer textRenderer, OrderedText text, int x, int y, int color) {
		return this.drawText(textRenderer, text, x, y, color, true);
	}

	public int drawText(FontRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow) {
		int i = textRenderer.drawString(text.asString(), x, y, color, shadow);
		return i;
	}

	public int drawTextWithShadow(FontRenderer textRenderer, Text text, int x, int y, int color) {
		return this.drawText(textRenderer, text, x, y, color, true);
	}

	public int drawText(FontRenderer textRenderer, Text text, int x, int y, int color, boolean shadow) {
		return this.drawText(textRenderer, text.asOrderedText(), x, y, color, shadow);
	}

	public void drawTextWrapped(FontRenderer textRenderer, String text, int x, int y, int width, int color) {
		this.drawText(textRenderer, textRenderer.trimStringToWidth(text, width), x, y, color, false);
		Objects.requireNonNull(textRenderer);
	}

	public int drawTextWithBackground(FontRenderer textRenderer, Text text, int x, int y, int width, int color) {
		int i = (int) this.client.gameSettings.chatOpacity;
		if (i != 0) {
			int j = 2;
			int var10001 = x - j;
			int var10002 = y - j;
			int var10003 = x + width + j;
			Objects.requireNonNull(textRenderer);
			this.fill(var10001, var10002, var10003, y + 9 + j, ColorHelper.Argb.mixColor(i, color));
		}

		return this.drawText(textRenderer, text, x, y, color, true);
	}

    public void drawGuiTexture(ResourceLocation texture, int x, int y, int width, int height) {
        this.drawGuiTexture(texture, x, y, 0, width, height);
    }

    public void drawGuiTexture(ResourceLocation texture, int x, int y, int z, int width, int height) {
        this.drawSprite(guiAtlasManager.getAtlasSprite(texture.toString()), x, y, z, width, height);
    }

    public void drawGuiTexture(ResourceLocation texture, int i, int j, int k, int l, int x, int y, int width, int height) {
        this.drawGuiTexture(texture, i, j, k, l, x, y, 0, width, height);
    }

    public void drawGuiTexture(ResourceLocation texture, int i, int j, int k, int l, int x, int y, int z, int width, int height) {
        this.drawSprite(guiAtlasManager.getAtlasSprite(texture.toString()), i, j, k, l, x, y, z, width, height);
    }

    private void drawSprite(TextureAtlasSprite sprite, int i, int j, int k, int l, int x, int y, int z, int width, int height) {
        if (width != 0 && height != 0) {
            this.drawTexturedQuad(EmiPort.id(sprite.getIconName()), x, x + width, y, y + height, z, sprite.getInterpolatedU((float)k / (float)i), sprite.getInterpolatedU((float)(k + width) / (float)i), sprite.getInterpolatedV((float)l / (float)j), sprite.getInterpolatedV((float)(l + height) / (float)j));
        }
    }

    private void drawSprite(TextureAtlasSprite sprite, int x, int y, int z, int width, int height) {
        if (width != 0 && height != 0) {
            this.drawTexturedQuad(EmiPort.id(sprite.getIconName()), x, x + width, y, y + height, z, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
        }
    }

	public void drawTexture(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
		this.drawTexture(texture, x, y, 0, (float)u, (float)v, width, height, 256, 256);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int z, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		this.drawTexture(texture, x, x + width, y, y + height, z, width, height, u, v, textureWidth, textureHeight);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		this.drawTexture(texture, x, x + width, y, y + height, 0, regionWidth, regionHeight, u, v, textureWidth, textureHeight);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		this.drawTexture(texture, x, y, width, height, u, v, width, height, textureWidth, textureHeight);
	}

	void drawTexture(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight) {
		this.drawTexturedQuad(texture, x1, x2, y1, y2, z, (u + 0.0F) / (float)textureWidth, (u + (float)regionWidth) / (float)textureWidth, (v + 0.0F) / (float)textureHeight, (v + (float)regionHeight) / (float)textureHeight);
	}

	void drawTexturedQuad(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
		RenderSystem.setShaderTexture(0, texture);
        EmiPort.setPositionTexShader();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(x1, y2, z).tex(u1, v2).endVertex();
        bufferBuilder.pos(x2, y2, z).tex(u2, v2).endVertex();
        bufferBuilder.pos(x2, y1, z).tex(u2, v1).endVertex();
        bufferBuilder.pos(x1, y1, z).tex(u1, v1).endVertex();
		EmiPort.draw(bufferBuilder);
	}

	public void drawTexturedQuad(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha) {
		RenderSystem.setShaderTexture(0, texture);
        EmiPort.setPositionTexShader();
		RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferBuilder.pos(x1, y2, z).tex(u1, v2).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x2, y2, z).tex(u2, v2).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x2, y1, z).tex(u2, v1).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x1, y1, z).tex(u1, v1).color(red, green, blue, alpha).endVertex();
        EmiPort.draw(bufferBuilder);
		RenderSystem.disableBlend();
	}

	public void drawItem(ItemStack stack, int x, int y) {
		if (stack == null) return;
		RetroEMI.instance.itemRenderer.zLevel += 100;
		RetroEMI.instance.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
		RetroEMI.instance.itemRenderer.zLevel -= 100;
		RenderSystem.defaultBlendFunc();
	}

	public void drawItemInSlot(FontRenderer fontRenderer, ItemStack stack, int x, int y) {
		RetroEMI.instance.itemRenderer.zLevel += 200;
		int count = stack.getCount();
		stack.setCount(1);
		RetroEMI.instance.itemRenderer.renderItemOverlayIntoGUI(fontRenderer, stack, x, y, "");
		stack.setCount(count);
		RetroEMI.instance.itemRenderer.zLevel -= 200;
	}

	public void drawTooltip(FontRenderer fontRenderer, List<Text> txt, int mouseX, int mouseY) {
		EmiRenderHelper.drawTooltip(client.currentScreen, EmiDrawContext.instance(), txt.stream().map(TooltipComponent::of).collect(Collectors.toList()), mouseX, mouseY);
	}
}
