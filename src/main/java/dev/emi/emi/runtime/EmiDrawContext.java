package dev.emi.emi.runtime;

import shim.com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.text.OrderedText;
import shim.net.minecraft.text.Text;

public class EmiDrawContext {
	private final Minecraft client =  Minecraft.getMinecraft();
	private static final EmiDrawContext INSTANCE = new EmiDrawContext();
	public final Gui context = new Gui();

	private EmiDrawContext() {}

	public static EmiDrawContext instance() {
		return INSTANCE;
	}

	public static EmiDrawContext wrap(DrawContext context) {
		return INSTANCE;
	}

	public DrawContext raw() {
		return DrawContext.INSTANCE;
	}

	public MatrixStack matrices() {
		return MatrixStack.INSTANCE;
	}

	public void push() {
		GL11.glPushMatrix();
	}

	public void pop() {
		GL11.glPopMatrix();
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int u, int v, int w, int h) {
		drawTexture(texture, x, y, w, h, u, v, w, h, 256, 256);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int z, float u, float v, int w, int h) {
		drawTexture(texture, x, y, z, u, v, w, h, 256, 256);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int z, float u, float v, int w, int h, int textureWidth, int textureHeight) {
		drawTexture(texture, x, y, z, w, h, u, v, w, h, textureWidth, textureHeight);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int w, int h, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		drawTexture(texture, x, y, 0, w, h, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
	}

	public void drawTexture(ResourceLocation texture, int x, int y, int z, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		client.getTextureManager().bindTexture(texture);
		float uM = 1 / (float) textureWidth;
		float vM = 1 / (float) textureHeight;
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, y + height, z, (u +  0) * uM, (v + regionHeight) * vM);
		tess.addVertexWithUV(x + width, y + height, z, (u + regionWidth) * uM, (v + regionHeight) * vM);
		tess.addVertexWithUV(x + width, y, z, (u + regionWidth) * uM, (v +  0) * vM);
		tess.addVertexWithUV(x, y, z, (u +  0) * uM, (v +  0) * vM);
		tess.draw();
	}

	public void fill(int x, int y, int width, int height, int color) {
		Gui.drawRect(x, y, x + width, y + height, color);
	}

	public void drawText(Text text, int x, int y) {
		drawText(text, x, y, -1);
	}

	public void drawText(Text text, int x, int y, int color) {
		client.fontRenderer.drawString(text.asString(), x, y, color);
	}

	public void drawText(OrderedText text, int x, int y, int color) {
		client.fontRenderer.drawString(text.asString(), x, y, color);
	}

	public void drawTextWithShadow(Text text, int x, int y) {
		drawTextWithShadow(text, x, y, -1);
	}

	public void drawTextWithShadow(Text text, int x, int y, int color) {
		client.fontRenderer.drawStringWithShadow(text.asString(), x, y, color);
	}

	public void drawTextWithShadow(OrderedText text, int x, int y, int color) {
		client.fontRenderer.drawStringWithShadow(text.asString(), x, y, color);
	}

	public void drawCenteredText(Text text, int x, int y) {
		drawCenteredText(text, x, y, -1);
	}

	public void drawCenteredText(Text text, int x, int y, int color) {
		client.fontRenderer.drawString(text.asString(), x - client.fontRenderer.getStringWidth(text.asString()) / 2, y, color);
	}

	public void drawCenteredTextWithShadow(Text text, int x, int y) {
		drawCenteredTextWithShadow(text, x, y, -1);
	}

	public void drawCenteredTextWithShadow(Text text, int x, int y, int color) {
		client.fontRenderer.drawStringWithShadow(text.asString(), x - client.fontRenderer.getStringWidth(text.asString()) / 2, y, color);
	}

	public void enableDepthTest() {
		RenderSystem.enableDepthTest();
	}

	public void disableDepthTest() {
		RenderSystem.disableDepthTest();
	}

	public void enableBlend() {
		RenderSystem.enableBlend();
	}

	public void disableBlend() {
		RenderSystem.disableBlend();
	}

	public void resetColor() {
		setColor(1f, 1f, 1f, 1f);
	}

	public void setColor(float r, float g, float b) {
		setColor(r, g, b, 1f);
	}

	public void setColor(float r, float g, float b, float a) {
		GL11.glColor4f(r, g, b, a);
	}

	public void drawStack(EmiIngredient stack, int x, int y) {
		stack.render(raw(), x, y, client.timer.renderPartialTicks);
	}

	public void drawStack(EmiIngredient stack, int x, int y, int flags) {
		drawStack(stack, x, y, client.timer.renderPartialTicks, flags);
	}

	public void drawStack(EmiIngredient stack, int x, int y, float delta, int flags) {
		stack.render(raw(), x, y, delta, flags);
	}
}
