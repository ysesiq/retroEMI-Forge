package com.rewindmc.retroemi.plugin;

import java.util.regex.Pattern;

import com.rewindmc.retroemi.RetroEMI;
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
	public static final Pattern CUSTOM_FORMAT_CODE = Pattern.compile("(?:§[0-9a-fA-F]){6}§x");

	public static void applyCustomFormatCodes(FontRenderer subject, String code, boolean shadow) {
		int color = 0;
		for (int j = 1; j < 12; j += 2) {
			color = color << 4 | Character.digit(code.charAt(j), 16);
		}
		if (shadow) color = (color & 0xFCFCFC) >> 2 | (color & 0xFF000000);
		EmiDrawContext context = EmiDrawContext.instance();
		context.setColor((color >> 16) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, ((FontRendererAccessor) subject).getAlpha());
	}

	// InventoryEffectRender
	public static final int EFFECT_WIDTH = 124;

	public static String getPotionAmplifier(PotionEffect effect) {
		return " " + RetroEMI.translate("enchantment.level." + (effect.getAmplifier() + 1));
	}

	public static void drawStatusEffectBackgrounds(DrawContext context, int x, int y, boolean wide) {
		if (wide) {
			context.drawTexture(GuiContainer.field_147001_a, x, y, 0, 166, 120, 32);
		} else {
			// split so it renders the edge properly
			context.drawTexture(GuiContainer.field_147001_a, x, y, 0, 166, 28, 32);
			context.drawTexture(GuiContainer.field_147001_a, x + 28, y, 116, 166, 4, 32);
		}
	}

	public static void drawStatusEffectDescriptions(int x, int y, PotionEffect potionEffect) {
		String potionName = RetroEMI.translate(potionEffect.getEffectName()) + getPotionAmplifier(potionEffect);
		client.fontRenderer.drawStringWithShadow(potionName, x + 10 + 18, y + 6, 16777215);
		String durationString = Potion.getDurationString(potionEffect);
		client.fontRenderer.drawStringWithShadow(durationString, x + 10 + 18, y + 16, 8355711);
	}

	public static void drawStatusEffectSprites(DrawContext context, int x, int y, PotionEffect potionEffect) {
		Potion potionType = Potion.potionTypes[potionEffect.getPotionID()];
		if (potionType.hasStatusIcon()) {
			int statusIconIndex = potionType.getStatusIconIndex();
			context.drawTexture(GuiContainer.field_147001_a, x + 6, y + 7, statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
		}
	}
}
