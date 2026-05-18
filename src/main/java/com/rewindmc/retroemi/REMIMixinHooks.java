package com.rewindmc.retroemi;

import dev.emi.emi.mixin.accessor.FontRendererAccessor;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import shim.net.minecraft.client.gui.DrawContext;

public class REMIMixinHooks {
	private static final Minecraft client = Minecraft.getMinecraft();

	// FontRenderer
	private static final char SECTION = '\u00a7';// §

	public static int applyCustomFormatCodes(FontRenderer subject, String str, boolean shadow, int i) {
		EmiDrawContext context = EmiDrawContext.instance();
		if (i + 14 <= str.length() && str.charAt(i) == SECTION && str.startsWith(SECTION + "x", i + 12)) {
			int color = 0;
			for (int j = 0; j < 6; j++) {
				int p = i + j * 2;
				int d = Character.digit(str.charAt(p + 1), 16);
				if (str.charAt(p) != SECTION || d < 0) return i;
				color = (color << 4) | d;
			}
			if (shadow) color = (color & 0xFCFCFC) >> 2 | (color & 0xFF000000);
			context.setColor((color >> 16) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, ((FontRendererAccessor) subject).getAlpha());
			return i + 12;
		}
		return i;
	}

	// InventoryEffectRender
	public static final int EFFECT_WIDTH = 124;

	public static String getPotionAmplifier(PotionEffect effect) {
		return switch (effect.getAmplifier()) {
			case 1 -> " " + RetroEMI.translate("enchantment.level.2");
			case 2 -> " " + RetroEMI.translate("enchantment.level.3");
			case 3 -> " " + RetroEMI.translate("enchantment.level.4");
			default -> "";
		};
	}

	public static void drawStatusEffectBackgrounds(DrawContext context, int x, int y, boolean wide) {
        if (wide) {
            context.drawTexture(GuiContainer.INVENTORY_BACKGROUND, x, y, 0, 166, 120, 32);
		} else {
			// split so it renders the edge properly
            context.drawTexture(GuiContainer.INVENTORY_BACKGROUND, x, y, 0, 166, 28, 32);
            context.drawTexture(GuiContainer.INVENTORY_BACKGROUND, x + 28, y, 116, 166, 4, 32);
		}
	}

	public static void drawStatusEffectDescriptions(int x, int y, PotionEffect potionEffect) {
        String potionName = RetroEMI.translate(potionEffect.getEffectName()) + getPotionAmplifier(potionEffect);
        client.fontRenderer.drawStringWithShadow(potionName, x + 10 + 18, y + 6, 16777215);
        String durationString = Potion.getPotionDurationString(potionEffect, 1.0F);
        client.fontRenderer.drawStringWithShadow(durationString, x + 10 + 18, y + 16, 8355711);
	}

	public static void drawStatusEffectSprites(DrawContext context, int x, int y, PotionEffect potionEffect) {
		Potion potionType = Potion.getPotionById(Potion.getIdFromPotion(potionEffect.getPotion()));
		if (potionType.hasStatusIcon()) {
			int statusIconIndex = potionType.getStatusIconIndex();
            context.drawTexture(GuiContainer.INVENTORY_BACKGROUND, x + 6, y + 7, statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
		}
	}
}
