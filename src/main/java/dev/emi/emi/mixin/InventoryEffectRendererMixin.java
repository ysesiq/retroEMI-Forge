package dev.emi.emi.mixin;

import java.util.Collection;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.input.Mouse;
import org.objectweb.asm.Opcodes;

import com.google.common.collect.Ordering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.rewindmc.retroemi.REMIMixinHooks;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.text.Text;

@Mixin(InventoryEffectRenderer.class)
public abstract class InventoryEffectRendererMixin extends GuiContainer {
	private InventoryEffectRendererMixin() { super(null); }

	@WrapOperation(method = "updateActivePotionEffects",
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;guiLeft:I",
			opcode = Opcodes.PUTFIELD
		)
	)
	private void conNotBeyond(InventoryEffectRenderer instance, int value, Operation<Void> original) {
		guiLeft = Math.max(guiLeft, REMIMixinHooks.EFFECT_WIDTH);
	}

	@Inject(at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawScreen(IIF)V"),
		method = "drawScreen")
	private void drawScreen(int mouseX, int mouseY, float par3, CallbackInfo ci) {
		if (EmiConfig.effectLocation == EffectLocation.TOP) {
			emi$drawCenteredEffects(DrawContext.INSTANCE, mouseX, mouseY);
		}
	}

	@ModifyVariable(at = @At(value = "INVOKE", target = "java/util/Collection.size()I", ordinal = 0),
		method = "drawActivePotionEffects", ordinal = 0)
	private Collection<PotionEffect> drawStatusEffects(Collection<PotionEffect> original) {
		if (EmiConfig.effectLocation == EffectLocation.TOP || EmiConfig.effectLocation == EffectLocation.HIDDEN) {
			return List.of();
		}
		return original;
	}

	private void emi$drawCenteredEffects(DrawContext raw, int mouseX, int mouseY) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		Collection<PotionEffect> effects = Ordering.natural().sortedCopy(this.mc.player.getActivePotionEffects());
		int size = effects.size();
		if (size == 0) {
			return;
		}
		boolean wide = size == 1;
		int y = this.guiTop - 34;
		if (((Object) this)  instanceof GuiContainerCreative) {
			y -= 28;
			if (((Object) this)  instanceof GuiContainerCreative && EmiAgnos.isForge()) {
				y -= 22;
			}
		}
		int xOff = 34;
		if (wide) {
			xOff = 122;
		} else if (size > 5) {
			xOff = (this.xSize - 32) / (size - 1);
		}
		int width = (size - 1) * xOff + (wide ? 120 : 32);
		int x = this.guiLeft + (this.xSize - width) / 2;
		PotionEffect hovered = null;
		int restoreY = this.ySize;
		try {
			this.ySize = y;
			for (PotionEffect inst : effects) {
				int ew = wide ? 120 : 32;
				context.push();
				context.resetColor();
				RenderSystem.disableLighting();
				RenderSystem.enableBlend();
				REMIMixinHooks.drawStatusEffectBackgrounds(context.raw(), x, y, wide);
				REMIMixinHooks.drawStatusEffectSprites(context.raw(), x, y, inst);
				if (wide) {
					REMIMixinHooks.drawStatusEffectDescriptions(x - width, y, inst);
				}
				if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
					hovered = inst;
				}
				x += xOff;
				context.pop();
			}
		} finally {
			this.ySize = restoreY;
		}
		if (hovered != null && size > 1) {
			List<Text> list = List.of(Text.translatable(hovered.getEffectName()).append(Text.literal(REMIMixinHooks.getPotionAmplifier(hovered))), Text.literal(Potion.getPotionDurationString(hovered, 1.0F)));
			context.raw().drawTooltip(mc.fontRenderer, list, mouseX, Math.max(mouseY, 16));
		}
	}

	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/Potion;shouldRenderInvText(Lnet/minecraft/potion/PotionEffect;)Z"),
		method = "drawActivePotionEffects")
	private boolean squishEffects(Potion instance, PotionEffect effect, Operation<Boolean> original) {
		return !EmiConfig.effectLocation.compressed;
	}

	@ModifyVariable(at = @At(value = "STORE", ordinal = 0),
		method = "drawActivePotionEffects", index = 1)
	private int changeEffectSpace(int original) {
		return switch (EmiConfig.effectLocation) {
			case RIGHT, RIGHT_COMPRESSED, HIDDEN -> this.guiLeft + this.xSize + 2;
			case TOP -> this.guiLeft;
			case LEFT_COMPRESSED -> this.guiLeft - 2 - 32;
			case LEFT -> original;
		};
	}

	@WrapOperation(method = "drawActivePotionEffects",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;drawTexturedModalRect(IIIIII)V", ordinal = 0))
	private void drawBackground(InventoryEffectRenderer instance, int x, int y, int u, int v, int width, int height, Operation<Void> original) {
		REMIMixinHooks.drawStatusEffectBackgrounds(DrawContext.INSTANCE, x, y, !EmiConfig.effectLocation.compressed);
	}

	@Inject(method = "drawActivePotionEffects", at = @At("TAIL"))
	private void drawTooltip(CallbackInfo ci, @Local(ordinal = 0) int effectX, @Local Collection<PotionEffect> effects) {
		EmiDrawContext context = EmiDrawContext.instance();
		if (EmiConfig.effectLocation.compressed && !effects.isEmpty()) {
			ScaledResolution sr = new ScaledResolution(this.mc);
			int width = sr.getScaledWidth();
			int height = sr.getScaledHeight();
			int mouseX = Mouse.getX() * width / this.mc.displayWidth;
			int mouseY = height - Mouse.getY() * height / this.mc.displayHeight - 1;
			PotionEffect hovered = null;
			int spacing = effects.size() > 5 ? 132 / (effects.size() - 1) : 33;
			int currentY = this.guiTop;
			for (PotionEffect effect : Ordering.natural().sortedCopy(effects)) {
				Potion potion = effect.getPotion();
				if (potion.shouldRender(effect)) {
					int ew = 32;
					if (mouseX >= effectX && mouseX < effectX + ew && mouseY >= currentY && mouseY < currentY + ew) {
						hovered = effect;
					}
					currentY += spacing;
				}
			}
			if (hovered != null && effects.size() > 1) {
				List<Text> list = List.of(Text.translatable(hovered.getEffectName()).append(Text.literal(REMIMixinHooks.getPotionAmplifier(hovered))), Text.literal(Potion.getPotionDurationString(hovered, 1.0F)));
				context.raw().drawTooltip(mc.fontRenderer, list, mouseX, Math.max(mouseY, 16));
			}
		}
	}
}
