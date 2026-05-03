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
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
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
	public static final DrawContext INSTANCE = new DrawContext(Minecraft.getMinecraft(), MatrixStack.INSTANCE);

	public DrawContext(Minecraft client, MatrixStack matrices) {
		this.client = client;
		this.matrices = matrices;
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

		float a = (float) (color >> 24 & 255) / 255.0F;
		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
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
		GlStateManager.enableTexture2D();
		RenderSystem.disableBlend();
	}

	public void fillGradient(int startX, int startY, int endX, int endY, int colorStart, int colorEnd) {
		this.fillGradient(startX, startY, endX, endY, 0, colorStart, colorEnd);
	}

	public void fillGradient(int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd) {
		float as = (float)(colorStart >> 24 & 255) / 255.0F;
		float rs = (float)(colorStart >> 16 & 255) / 255.0F;
		float gs = (float)(colorStart >> 8 & 255) / 255.0F;
		float bs = (float)(colorStart & 255) / 255.0F;
		float ae = (float)(colorEnd >> 24 & 255) / 255.0F;
		float re = (float)(colorEnd >> 16 & 255) / 255.0F;
		float ge = (float)(colorEnd >> 8 & 255) / 255.0F;
		float be = (float)(colorEnd & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		RenderSystem.enableBlend();
		GlStateManager.disableAlpha();
		RenderSystem.defaultBlendFunc();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		bufferBuilder.pos(endX, startY, z).color(rs, gs, bs, as).endVertex();
		bufferBuilder.pos(startX, startY, z).color(rs, gs, bs, as).endVertex();
		bufferBuilder.pos(startX, endY, z).color(re, ge, be, ae).endVertex();
		bufferBuilder.pos(endX, endY, z).color(re, ge, be, ae).endVertex();
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
		this.drawSprite(texture, x, y, z, width, height);
	}

	public void drawGuiTexture(ResourceLocation texture, int i, int j, int k, int l, int x, int y, int width, int height) {
		this.drawGuiTexture(texture, i, j, k, l, x, y, 0, width, height);
	}

	public void drawGuiTexture(ResourceLocation texture, int i, int j, int k, int l, int x, int y, int z, int width, int height) {
		this.drawSprite(texture, i, j, k, l, x, y, z, width, height);
	}

	private void drawSprite(ResourceLocation sprite, int u1, int v1, int u2, int v2, int x, int y, int z, int width, int height) {
		if (width != 0 && height != 0) {
			float scale = 1.0f / 256.0f;
			this.drawTexturedQuad(sprite, x, x + width, y, y + height, z, u1 * scale, (u1 + u2) * scale, v1 * scale, (v1 + v2) * scale);
		}
	}

	private void drawSprite(ResourceLocation sprite, int x, int y, int z, int width, int height) {
		if (width != 0 && height != 0) {
			float scale = 1.0f / 256.0f;
			this.drawTexturedQuad(sprite, x, x + width, y, y + height, z, 0 * scale, width * scale, 0 * scale, height * scale);
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

	void drawTexture(
		ResourceLocation texture, int x1, int x2, int y1, int y2, int z, int regionWidth, int regionHeight, float u, float v, int textureWidth, int textureHeight
	) {
		this.drawTexturedQuad(
			texture,
			x1,
			x2,
			y1,
			y2,
			z,
			(u + 0.0F) / (float)textureWidth,
			(u + (float)regionWidth) / (float)textureWidth,
			(v + 0.0F) / (float)textureHeight,
			(v + (float)regionHeight) / (float)textureHeight
		);
	}

	void drawTexturedQuad(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
		RenderSystem.setShaderTexture(0, texture);
		EmiPort.setPositionTexShader();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		bufferBuilder.pos(x1, y1, z).tex(u1, v1).endVertex();
		bufferBuilder.pos(x1, y2, z).tex(u1, v2).endVertex();
		bufferBuilder.pos(x2, y2, z).tex(u2, v2).endVertex();
		bufferBuilder.pos(x2, y1, z).tex(u2, v1).endVertex();
		EmiPort.draw(bufferBuilder);
	}

	void drawTexturedQuad(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha) {
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

//	public void drawItem(ItemStack item, int x, int y) {
//		this.drawItem(this.client.player, this.client.world, item, x, y, 0);
//	}
//
//	public void drawItem(ItemStack stack, int x, int y, int seed) {
//		this.drawItem(this.client.player, this.client.world, stack, x, y, seed);
//	}
//
//	public void drawItem(ItemStack stack, int x, int y, int seed, int z) {
//		this.drawItem(this.client.player, this.client.world, stack, x, y, seed, z);
//	}
//
//	public void drawItemWithoutEntity(ItemStack stack, int x, int y) {
//		this.drawItemWithoutEntity(stack, x, y, 0);
//	}
//
//	public void drawItemWithoutEntity(ItemStack stack, int x, int y, int seed) {
//		this.drawItem(null, this.client.world, stack, x, y, seed);
//	}
//
//	public void drawItem(EntityLivingBase entity, ItemStack stack, int x, int y, int seed) {
//		this.drawItem(entity, entity.getEntityWorld(), stack, x, y, seed);
//	}
//
//	private void drawItem(@Nullable EntityLivingBase entity, @Nullable World world, ItemStack stack, int x, int y, int seed) {
//		this.drawItem(entity, world, stack, x, y, seed, 0);
//	}
//
//	private void drawItem(@Nullable EntityLivingBase entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int z) {
//		if (!stack.isEmpty()) {
//			IBakedModel bakedModel = this.client.getRenderItem().getItemModelWithOverrides(stack, world, entity);
//			this.matrices.push();
////			this.matrices.translate((float)(x + 8), (float)(y + 8), (float)(150 + z));
//
//			try {
////				this.matrices.scale(16.0F, -16.0F, 16.0F);
//				boolean bl = !bakedModel.isGui3d();
//				if (bl) {
//					RenderHelper.disableStandardItemLighting();
//				}
//
//				this.client.getRenderItem().renderItemIntoGUI(stack, x, y);
//				if (bl) {
//					RenderHelper.enableGUIStandardItemLighting();
//				}
//			} catch (Throwable throwable) {
//				CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Rendering item");
//				CrashReportCategory crashReportSection = crashReport.makeCategory("Item being rendered");
//				crashReportSection.addDetail("Item Type", () -> String.valueOf(stack.getItem()));
//				crashReportSection.addDetail("Item Components", () -> String.valueOf(stack.getTagCompound()));
//				crashReportSection.addDetail("Item Foil", () -> String.valueOf(stack.hasEffect()));
//				throw new ReportedException(crashReport);
//			}
//
//			this.matrices.pop();
//		}
//	}
//
//	public void drawItemInSlot(FontRenderer textRenderer, ItemStack stack, int x, int y) {
//		this.drawItemInSlot(textRenderer, stack, x, y, null);
//	}
//
//	public void drawItemInSlot(FontRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countOverride) {
//		if (!stack.isEmpty()) {
//			this.matrices.push();
//			if (stack.getCount() != 1 || countOverride != null) {
//				String string = countOverride == null ? String.valueOf(stack.getCount()) : countOverride;
//				this.matrices.translate(0.0F, 0.0F, 200.0F);
//				this.drawText(textRenderer, string, x + 19 - 2 - textRenderer.getStringWidth(string), y + 6 + 3, 16777215, true);
//			}
//
//			if (stack.getItem().showDurabilityBar(stack)) {
//				RenderSystem.disableDepthTest();
//				int i = Math.round(13.0F - (float) stack.getItem().getDurabilityForDisplay(stack) * 13.0F);
//				int j = stack.getItem().getRGBDurabilityForDisplay(stack);
//				int k = x + 2;
//				int l = y + 13;
//				this.fill(k, l, k + 13, l + 2, 0xff000000);
//				this.fill(k, l, k + i, l + 1, j | 0xff000000);
//				RenderSystem.enableDepthTest();
//			}
//
//			EntityPlayerSP clientPlayerEntity = this.client.player;
//			float f = clientPlayerEntity == null ? 0.0F : clientPlayerEntity.getCooldownTracker().getCooldown(stack.getItem(), this.client.getRenderPartialTicks());
//			if (f > 0.0F) {
//				int k = y + MathHelper.floor(16.0F * (1.0F - f));
//				int l = k + MathHelper.ceil(16.0F * f);
//				this.fill(x, k, x + 16, l, Integer.MAX_VALUE);
//			}
//
//			this.matrices.pop();
//		}
//	}

	public void drawItem(ItemStack stack, int x, int y) {
		if (stack == null) return;
		client.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
		RenderSystem.defaultBlendFunc();
	}

	public void drawItemInSlot(FontRenderer fontRenderer, ItemStack stack, int x, int y) {
		int count = stack.getCount();
		stack.setCount(1);
		client.getRenderItem().renderItemOverlayIntoGUI(fontRenderer, stack, x, y, "");
		stack.setCount(count);
	}

	public void drawTooltip(FontRenderer fontRenderer, List<Text> txt, int mouseX, int mouseY) {
		EmiRenderHelper.drawTooltip(client.currentScreen, EmiDrawContext.instance(), txt.stream().map(TooltipComponent::of).collect(Collectors.toList()), mouseX, mouseY);
	}
}
