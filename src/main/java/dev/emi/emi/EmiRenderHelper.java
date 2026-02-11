package dev.emi.emi;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraftforge.fluids.Fluid;

import com.google.common.collect.Lists;
import shim.com.mojang.blaze3d.systems.RenderSystem;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import shim.net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import shim.net.minecraft.client.gui.tooltip.TextTooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TooltipPositioner;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.client.util.math.Vec2i;
import shim.net.minecraft.text.Style;
import shim.net.minecraft.text.Text;
import net.minecraft.util.ResourceLocation;

public class EmiRenderHelper {
	public static final DecimalFormat TEXT_FORMAT = new DecimalFormat("#,###.##");
	public static final Text EMPTY_TEXT = EmiPort.literal("");
	public static final Minecraft CLIENT = Minecraft.getMinecraft();
	public static final ResourceLocation WIDGETS = EmiPort.id("emi", "textures/gui/widgets.png");
	public static final ResourceLocation BUTTONS = EmiPort.id("emi", "textures/gui/buttons.png");
	public static final ResourceLocation BACKGROUND = EmiPort.id("emi", "textures/gui/background.png");
	public static final ResourceLocation GRID = EmiPort.id("emi", "textures/gui/grid.png");
	public static final ResourceLocation DASH = EmiPort.id("emi", "textures/gui/dash.png");
	public static final ResourceLocation CONFIG = EmiPort.id("emi", "textures/gui/config.png");
	public static final ResourceLocation PIECES = EmiPort.id("emi", "textures/gui/pieces.png");

	public static void drawNinePatch(EmiDrawContext context, ResourceLocation texture, int x, int y, int w, int h, int u, int v, int cornerLength, int centerLength) {
		int cor = cornerLength;
		int cen = centerLength;
		int corcen = cor + cen;
		int innerWidth = w - cornerLength * 2;
		int innerHeight = h - cornerLength * 2;
		int coriw = cor + innerWidth;
		int corih = cor + innerHeight;
		// TL
		context.drawTexture(texture, x,         y,         cor,        cor,         u,          v,          cor, cor, 256, 256);
		// T
		context.drawTexture(texture, x + cor,   y,         innerWidth, cor,         u + cor,    v,          cen, cor, 256, 256);
		// TR
		context.drawTexture(texture, x + coriw, y,         cor,        cor,         u + corcen, v,          cor, cor, 256, 256);
		// L
		context.drawTexture(texture, x,         y + cor,   cor,        innerHeight, u,          v + cor,    cor, cen, 256, 256);
		// C
		context.drawTexture(texture, x + cor,   y + cor,   innerWidth, innerHeight, u + cor,    v + cor,    cen, cen, 256, 256);
		// R
		context.drawTexture(texture, x + coriw, y + cor,   cor,        innerHeight, u + corcen, v + cor,    cor, cen, 256, 256);
		// BL
		context.drawTexture(texture, x,         y + corih, cor,        cor,         u,          v + corcen, cor, cor, 256, 256);
		// B
		context.drawTexture(texture, x + cor,   y + corih, innerWidth, cor,         u + cor,    v + corcen, cen, cor, 256, 256);
		// BR
		context.drawTexture(texture, x + coriw, y + corih, cor,        cor,         u + corcen, v + corcen, cor, cor, 256, 256);
	}

	public static void drawTintedSprite(MatrixStack matrices, String sheet, int icon, int color, int x, int y, int xOff, int yOff, int width, int height) {
		EmiPort.setPositionColorTexShader();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().renderEngine.getTexture(EmiPort.id		("minecraft", sheet));
		RenderSystem.enableBlend();

		float r = ((color >> 16) & 255) / 256f;
		float g = ((color >> 8) & 255) / 256f;
		float b = (color & 255) / 256f;

		float minU = (float) (icon % 16 * 16 + 0) / 256.0F;
		float maxU = (float) (icon % 16 * 16 + 16) / 256.0F;
		float minV = (float) (icon / 16 * 16 + 0) / 256.0F;
		float maxV = (float) (icon / 16 * 16 + 16) / 256.0F;

		RenderSystem.setShaderColor(r, g, b, 1);

		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		float xMin = (float) x;
		float yMin = (float) y;
		float xMax = xMin + width;
		float yMax = yMin + height;
		float uSpan = maxU - minU;
		float vSpan = maxV - minV;
		float uMin = minU + uSpan / 16 * xOff;
		float vMin = minV + vSpan / 16 * yOff;
		float uMax = maxU - uSpan / 16 * (16 - (width + xOff));
		float vMax = maxV - vSpan / 16 * (16 - (height + yOff));
		tess.addVertexWithUV(xMin, yMax, 1, uMin, vMax);
		tess.addVertexWithUV(xMax, yMax, 1, uMax, vMax);
		tess.addVertexWithUV(xMax, yMin, 1, uMax, vMin);
		tess.addVertexWithUV(xMin, yMin, 1, uMin, vMin);
		tess.draw();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public static void drawScroll(EmiDrawContext context, int x, int y, int width, int height, int progress, int total, int color) {
		if (total <= 1) {
			return;
		}
		int start = x + width * progress / total;
		int end = start + Math.max(width / total, 1);
		if (progress == total - 1) {
			end = x + width;
			start = end - Math.max(width / total, 1);
		}
		context.fill(start, y, end - start, height, color);
	}

	public static Text getEmiText() {
		return
			EmiPort.append(
				EmiPort.append(
					EmiPort.literal("E", Style.EMPTY.withColor(0xeb7bfc)),
					EmiPort.literal("M", Style.EMPTY.withColor(0x7bfca2))),
				EmiPort.literal("I", Style.EMPTY.withColor(0x7bebfc)));
	}

	public static Text getPageText(int page, int total, int maxWidth) {
		Text text = EmiPort.translatable("emi.page", page, total);
		if (CLIENT.fontRenderer.getStringWidth(text.asString()) > maxWidth) {
			text = EmiPort.translatable("emi.page.short", page, total);
			if (CLIENT.fontRenderer.getStringWidth(text.asString()) > maxWidth) {
				text = EmiPort.literal("" + page);
				if (CLIENT.fontRenderer.getStringWidth(text.asString()) > maxWidth) {
					text = EmiPort.literal("");
				}
			}
		}
		return text;
	}

	public static void drawLeftTooltip(GuiScreen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y) {
		drawTooltip(screen, context, components, x, y, screen.width / 2 - 16,
			(screen2, mouseX, mouseY, tooltipWidth, tooltipHeight) -> {
				Vec2i pos = new Vec2i( mouseX + 12, mouseY - 12);
				pos.x = Math.max(pos.x - 24 - tooltipWidth, 4);
				if (pos.y + tooltipHeight + 3 > screen.height) {
					pos.y = screen.height - tooltipHeight - 3;
				}
				return pos;
		});
	}

	public static void drawTooltip(GuiScreen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y) {
		drawTooltip(screen, context, components, x, y, screen.width / 2 - 16);
	}

	public static void drawTooltip(GuiScreen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y, int maxWidth) {
		drawTooltip(screen, context, components, x, y, maxWidth, HoveredTooltipPositioner.INSTANCE);
	}

	public static void drawTooltip(GuiScreen screen, EmiDrawContext context, List<TooltipComponent> components, int x, int y, int maxWidth, TooltipPositioner positioner) {
		if (components.isEmpty()) {
			return;
		}
		y = Math.max(16, y);
		// Some mods assume this list will be mutable, oblige them
		List<TooltipComponent> mutable = Lists.newArrayList();
		int wrapWidth = Math.max(components.stream()
			.map(c -> c instanceof TextTooltipComponent ? 0 : c.getWidth(CLIENT.fontRenderer))
			.max(Integer::compare).orElse(0), maxWidth);
		for (TooltipComponent comp : components) {
			if (comp instanceof TextTooltipComponent ottc && ottc.getWidth(CLIENT.fontRenderer) > wrapWidth) {
				try {
					for (String line : (List<String>) CLIENT.fontRenderer.listFormattedStringToWidth(ottc.getText(), wrapWidth)) {
						mutable.add(TooltipComponent.of(Text.literal(line)));
					}
				} catch (Exception e) {
					EmiLog.error("Error converting text", e);
					mutable.add(comp);
				}
			} else {
				mutable.add(comp);
			}
		}
        context.enableDepthTest();
		RenderSystem.disableLighting();
		EmiPort.setPositionTexShader();
		context.resetColor();
		RetroEMI.renderModernTooltip(screen, mutable, x, y, maxWidth, positioner);
	}

	public static void drawSlotHightlight(EmiDrawContext context, int x, int y, int w, int h) {
		context.push();
		context.matrices().translate(0, 0, 200);
		RenderSystem.colorMask(true, true, true, false);
		context.fill(x, y, w, h, -2130706433);
		RenderSystem.colorMask(true, true, true, true);
		context.pop();
	}

	public static Text getAmountText(EmiIngredient stack) {
		return getAmountText(stack, stack.getAmount());
	}

	public static Text getAmountText(EmiIngredient stack, long amount) {
		if (stack.isEmpty() || amount == 0) {
			return EMPTY_TEXT;
		}
		if (stack.getEmiStacks().get(0).getKey() instanceof Fluid) {
			return getFluidAmount(amount);
		}
		return EmiPort.literal(TEXT_FORMAT.format(amount));
	}

	public static Text getAmountText(EmiIngredient stack, double amount) {
		if (stack.isEmpty() || amount == 0) {
			return EMPTY_TEXT;
		}
		if (stack.getEmiStacks().get(0).getKey() instanceof Fluid) {
			return EmiConfig.fluidUnit.translate(amount);
		}
		return EmiPort.literal(TEXT_FORMAT.format(amount));
	}

	public static Text getFluidAmount(long amount) {
		return EmiConfig.fluidUnit.translate(amount);
	}

	public static int getAmountOverflow(Text amount) {
		int width = CLIENT.fontRenderer.getStringWidth(amount.asString());
		if (width > 14) {
			return width - 14;
		} else {
			return 0;
		}
	}

	public static void renderAmount(EmiDrawContext context, int x, int y, Text amount) {
		context.push();
		context.matrices().translate(0, 0, 200);
		int tx = x + 17 - Math.min(14, CLIENT.fontRenderer.getStringWidth(amount.asString()));
		context.drawTextWithShadow(amount, tx, y + 9, -1);
		context.pop();
	}

	public static void renderIngredient(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		context.enableDepthTest();
		context.push();
		context.matrices().translate(0, 0, 200);
		RenderSystem.setShaderTexture(0, EmiRenderHelper.WIDGETS);
		context.drawTexture(WIDGETS, x, y, 8, 252, 4, 4);
		context.pop();
	}

	public static void renderTag(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		if (ingredient.getEmiStacks().size() > 1) {
			context.enableDepthTest();
			context.push();
			context.matrices().translate(0, 0, 200);
			context.drawTexture(WIDGETS, x, y + 12, 0, 252, 4, 4);
			context.pop();
		}
	}

	public static void renderRemainder(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		for (EmiStack stack : ingredient.getEmiStacks()) {
			EmiStack remainder = stack.getRemainder();
			if (!remainder.isEmpty()) {
				if (remainder.equals(ingredient)) {
					renderCatalyst(ingredient, context, x, y);
				} else {
					context.push();
					context.matrices().translate(0, 0, 200);
					context.enableDepthTest();
					context.drawTexture(WIDGETS, x + 12, y, 4, 252, 4, 4);
					context.pop();
				}
				return;
			}
		}
	}

	public static void renderCatalyst(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		context.enableDepthTest();
		context.push();
		context.matrices().translate(0, 0, 200);
		context.drawTexture(WIDGETS, x + 12, y, 12, 252, 4, 4);
		context.pop();
		return;
	}

	public static void renderRecipeFavorite(EmiIngredient ingredient, EmiDrawContext context, int x, int y) {
		context.push();
		context.matrices().translate(0, 0, 200);
		context.enableDepthTest();
		context.drawTexture(WIDGETS, x + 12, y, 16, 252, 4, 4);
		context.pop();
		return;
	}

	public static void renderRecipeBackground(EmiRecipe recipe, EmiDrawContext context, int x, int y) {
		context.resetColor();
		EmiRenderHelper.drawNinePatch(context, BACKGROUND, x, y, recipe.getDisplayWidth() + 8, recipe.getDisplayHeight() + 8, 27, 0, 4, 1);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void renderRecipe(EmiRecipe recipe, EmiDrawContext context, int x, int y, boolean showMissing, int overlayColor) {
		try {
			renderRecipeBackground(recipe, context, x, y);

			List<Widget> widgets = Lists.newArrayList();
			WidgetHolder holder = new WidgetHolder() {

				public int getWidth() {
					return recipe.getDisplayWidth();
				}

				public int getHeight() {
					return recipe.getDisplayHeight();
				}

				public <T extends Widget> T add(T widget) {
					widgets.add(widget);
					return widget;
				}
			};

			context.push();
			context.matrices().translate(x + 4, y + 4, 0);

			recipe.addWidgets(holder);
			float delta = CLIENT.timer.renderPartialTicks;
			for (Widget widget : widgets) {
				widget.render(context.raw(), -1000, -1000, delta);
			}
			if (overlayColor != -1) {
				context.fill(-1, -1, recipe.getDisplayWidth() + 2, recipe.getDisplayHeight() + 2, overlayColor);
			}

			if (showMissing) {
				GuiContainer hs = EmiApi.getHandledScreen();
				EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(recipe, hs);
				if (handler != null) {
					handler.render(recipe, new EmiCraftContext(hs, handler.getInventory(hs), EmiCraftContext.Type.FILL_BUTTON), widgets, context.raw());
				} else if (EmiScreenManager.lastPlayerInventory != null) {
					StandardRecipeHandler.renderMissing(recipe, EmiScreenManager.lastPlayerInventory, widgets, context.raw());
				}
			}

			context.pop();

			// Force translucency to match that of the recipe background
            context.disableBlend();
			RenderSystem.colorMask(false, false, false, true);
            context.disableDepthTest();
			renderRecipeBackground(recipe, context, x, y);
			context.enableDepthTest();
			RenderSystem.colorMask(true, true, true, true);
			// Blend should be off by default
		} catch (Throwable e) {
			EmiLog.error("Error rendering recipe", e);
		}
	}
}
