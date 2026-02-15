package dev.emi.emi.mixin.early.minecraft.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import com.rewindmc.retroemi.RetroEMI;
import shim.java.List;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import shim.net.minecraft.text.Text;
import org.lwjgl.input.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;;

@Mixin(value = InventoryEffectRenderer.class, priority = 2000)
public abstract class InventoryEffectRendererMixin extends GuiContainer {
    @Shadow private boolean field_147045_u;

    @Unique private final int EFFECT_WIDTH = 124;

    public InventoryEffectRendererMixin(Container par1Container) {
        super(par1Container);
    }

    @WrapOperation(method = "initGui",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;guiLeft:I",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void conNotBeyond(InventoryEffectRenderer instance, int value, Operation<Void> original) {
        guiLeft = Math.max(guiLeft, EFFECT_WIDTH);
    }

    @Inject(method = "initGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;initGui()V", shift = At.Shift.AFTER), cancellable = true)
    private void initGui(CallbackInfo ci) {
        if (EmiConfig.effectLocation == EffectLocation.TOP || EmiConfig.effectLocation == EffectLocation.HIDDEN) {
            this.field_147045_u = false;
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawScreen(IIF)V", shift = At.Shift.AFTER), cancellable = true)
    private void drawScreen(int mouseX, int mouseY, float par3, CallbackInfo ci) {
        if (EmiConfig.effectLocation == EffectLocation.TOP) {
            drawCenteredEffects(mouseX, mouseY);
            ci.cancel();
        }
    }

    @Inject(method = "func_147044_g", at = @At("HEAD"), cancellable = true)
    private void displayDebuffEffects(CallbackInfo ci) {
        ci.cancel();

        Minecraft client = Minecraft.getMinecraft();
        ScaledResolution scaledresolution = new ScaledResolution(client, client.displayWidth, client.displayHeight);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / client.displayWidth;
        int mouseY = height - Mouse.getY() * height / client.displayHeight - 1;
        int debuffY = this.guiTop;
        int debuffX = changeEffectSpace(this.guiLeft - 124);
        boolean wide = !EmiConfig.effectLocation.compressed;
        Collection<PotionEffect> activePotionEffects = this.mc.thePlayer.getActivePotionEffects();
        int num_effects = activePotionEffects.size();

        if (num_effects > 0 && EmiConfig.effectLocation != EffectLocation.HIDDEN) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableLighting();
            RenderSystem.enableBlend();
            int spacing = 33;

            if (num_effects > 5 && EmiConfig.effectLocation != EffectLocation.HIDDEN && EmiConfig.effectLocation != EffectLocation.TOP) {
                spacing = 132 / (num_effects - 1);
            }

            PotionEffect hovered = null;

            for (Iterator<PotionEffect> iterator = activePotionEffects.iterator(); iterator.hasNext(); debuffY += spacing) {
                PotionEffect potionEffect = iterator.next();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(field_147001_a);
                drawStatusEffectBackgrounds(debuffX, debuffY, wide);

                int ew = wide ? 120 : 32;
                if (mouseX >= debuffX && mouseX < debuffX + ew && mouseY >= debuffY && mouseY < debuffY + 32) {
                    hovered = potionEffect;
                }
                drawPotionIcon(debuffX, debuffY, potionEffect);
                drawStatusEffectDescriptions(debuffX, debuffY, potionEffect, wide);
            }
            if (!wide) {
                renderTooltip(mouseX, mouseY, hovered);
            }
        }
    }

    @Unique
    private void drawCenteredEffects(int mouseX, int mouseY) {
        EmiDrawContext context = EmiDrawContext.instance();
        context.resetColor();
        Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
        int size = effects.size();
        if (size == 0) {
            return;
        }
        boolean wide = size == 1;
        int y = this.guiTop - 34;
        if (((InventoryEffectRenderer) (Object) this) instanceof GuiContainerCreative) {
            y -= 28;
            if (((InventoryEffectRenderer) (Object) this) instanceof GuiContainerCreative && EmiAgnos.isForge()) {
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
                this.mc.getTextureManager().bindTexture(field_147001_a);
                drawStatusEffectBackgrounds(x, y, wide);
                drawPotionIcon(x, y, inst);
                if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
                    hovered = inst;
                }
                x += xOff;
                drawStatusEffectDescriptions(x - width, y, inst, wide);
                context.pop();
            }
        } finally {
            this.ySize = restoreY;
        }
        if (size > 1) {
            renderTooltip(mouseX, mouseY, hovered);
        }
    }

    @Unique
    private void renderTooltip(int mouseX, int mouseY, PotionEffect effect) {
        if (effect != null) {
            String amplifier = getPotionAmplifier(effect);
            TooltipComponent name = TooltipComponent.of(Text.translatable(effect.getEffectName()).append(Text.literal(amplifier)));
            TooltipComponent duration = TooltipComponent.of(Text.literal(Potion.getDurationString(effect)));
            EmiRenderHelper.drawTooltip((InventoryEffectRenderer) (Object) this, EmiDrawContext.instance(), List.of(name, duration), mouseX, Math.max(mouseY, 16));
        }
    }

    @Unique
    private String getPotionAmplifier(PotionEffect effect) {
        return switch (effect.getAmplifier()) {
            case 1 -> " " + RetroEMI.translate("enchantment.level.2");
            case 2 -> " " + RetroEMI.translate("enchantment.level.3");
            case 3 -> " " + RetroEMI.translate("enchantment.level.4");
            default -> "";
        };
    }

    @Unique
    private void drawStatusEffectBackgrounds(int x, int y, boolean wide) {
        if (wide) {
            this.drawTexturedModalRect(x, y, 0, 166, 120, 32);
        } else {
            //split so it renders the edge properly
            this.drawTexturedModalRect(x, y, 0, 166, 28, 32);
            this.drawTexturedModalRect(x + 28, y, 116, 166, 4, 32);
        }
    }

    @Unique
    private void drawStatusEffectDescriptions(int x, int y, PotionEffect potionEffect, boolean wide) {
        if (wide) {
            String potionName = RetroEMI.translate(potionEffect.getEffectName()) + getPotionAmplifier(potionEffect);
            this.fontRendererObj.drawStringWithShadow(potionName, x + 10 + 18, y + 6, 16777215);
            String durationString = Potion.getDurationString(potionEffect);
            this.fontRendererObj.drawStringWithShadow(durationString, x + 10 + 18, y + 16, 8355711);
        }
    }

    @Unique
    private void drawPotionIcon(int x, int y, PotionEffect potionEffect) {
        Potion potionType = Potion.potionTypes[potionEffect.getPotionID()];
        if (potionType.hasStatusIcon()) {
            int statusIconIndex = potionType.getStatusIconIndex();
            this.drawTexturedModalRect(x + 6, y + 7, statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
        }
    }

    @Unique
    private int changeEffectSpace(int original) {
        return switch (EmiConfig.effectLocation) {
            case RIGHT, RIGHT_COMPRESSED, HIDDEN -> this.guiLeft + this.xSize + 2;
            case TOP -> this.guiLeft;
            case LEFT_COMPRESSED -> this.guiLeft - 2 - 32;
            case LEFT -> original;
        };
    }
}
