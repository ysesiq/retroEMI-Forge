package com.rewindmc.retroemi;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.mixin.accessor.CraftingManagerAccessor;
import dev.emi.emi.mixin.accessor.GuiContainerAccessor;
import dev.emi.emi.mixinsupport.inject_interface.EmiGuiContainer;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.text.Text;

public class REMIMixinHooks {
	static Minecraft client = Minecraft.getMinecraft();

	public static void drawSlot(Slot slot) {
		EmiDrawContext context = EmiDrawContext.instance();
		if (EmiScreenManager.search.highlight) {
			EmiSearch.CompiledQuery query = EmiSearch.compiledQuery;
			if (query != null && !query.test(EmiStack.of(slot.getStack()))) {
				context.push();
				context.matrices().translate(0, 0, 300);
				context.fill(slot.xDisplayPosition - 1, slot.yDisplayPosition - 1, 18, 18, 0x77000000);
				context.pop();
			}
		}
	}

	//SlotCrafting
	public static void onCrafting(EntityPlayer thePlayer, IInventory craftMatrix) {
		World world = thePlayer.worldObj;
		if (world.isRemote) {
			try {
				InventoryCrafting inv = (InventoryCrafting) craftMatrix;
				List<IRecipe> list = ((CraftingManagerAccessor) CraftingManager.getInstance()).getRecipes();
				for (IRecipe r : list) {
					if (r.matches(inv, client.theWorld)) {
						ResourceLocation id = EmiPort.getId(r);
						EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
						if (recipe != null) {
							EmiSidebars.craft(recipe);
							return;
						}
					}
				}
			} catch (Throwable t) {}
		}
	}

	//FontRenderer
	public static int applyCustomFormatCodes(FontRenderer subject, String str, boolean shadow, int i) {
        EmiDrawContext context = EmiDrawContext.instance();
		if (str.charAt(i + 1) == 'x') {
			int next = str.indexOf(String.valueOf('\u00a7') + "x", i + 1);
			if (next != -1) {
				String s = str.substring(i + 1, next);
				int color = Integer.parseInt(s.replace(String.valueOf('\u00a7'), "").substring(1), 16);
				if (shadow) {
					color = (color & 16579836) >> 2 | color & -16777216;
				}
				subject.textColor = color;
                context.setColor((color >> 16) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, subject.alpha);
				i += s.length()+1;
			}
		}
		return i;
	}

    //InventoryEffectRender

    public static final int EFFECT_WIDTH = 124;

    public static void drawEffects(InventoryEffectRenderer screen) {
        Minecraft client = Minecraft.getMinecraft();
        ScaledResolution scaledresolution = new ScaledResolution(client, client.displayWidth, client.displayHeight);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / client.displayWidth;
        int mouseY = height - Mouse.getY() * height / client.displayHeight - 1;
        int debuffY = ((GuiContainerAccessor) screen).getGuiTop();
        int debuffX = changeEffectSpace(screen, ((GuiContainerAccessor) screen).getGuiLeft() - EFFECT_WIDTH);
        boolean wide = !EmiConfig.effectLocation.compressed;
        Collection<PotionEffect> activePotionEffects = client.thePlayer.getActivePotionEffects();
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
                client.getTextureManager().bindTexture(((GuiContainerAccessor) screen).getInventoryResource());
                drawStatusEffectBackgrounds(screen, debuffX, debuffY, wide);

                int ew = wide ? 120 : 32;
                if (mouseX >= debuffX && mouseX < debuffX + ew && mouseY >= debuffY && mouseY < debuffY + 32) {
                    hovered = potionEffect;
                }
                drawPotionIcon(screen, debuffX, debuffY, potionEffect);
                drawStatusEffectDescriptions(screen, debuffX, debuffY, potionEffect, wide);
            }
            if (!wide) {
                renderTooltip(screen, mouseX, mouseY, hovered);
            }
        }
    }

    public static void drawCenteredEffects(InventoryEffectRenderer screen, int mouseX, int mouseY) {
        EmiDrawContext context = EmiDrawContext.instance();
        context.resetColor();
        Collection<PotionEffect> effects = client.thePlayer.getActivePotionEffects();
        int size = effects.size();
        if (size == 0) {
            return;
        }
        boolean wide = size == 1;
        int y = ((GuiContainerAccessor) screen).getGuiTop() - 34;
        if (screen instanceof GuiContainerCreative) {
            y -= 28;
            if (screen instanceof GuiContainerCreative && EmiAgnos.isForge()) {
                y -= 22;
            }
        }
        int xOff = 34;
        if (wide) {
            xOff = 122;
        } else if (size > 5) {
            xOff = (((GuiContainerAccessor) screen).getXSize() - 32) / (size - 1);
        }
        int width = (size - 1) * xOff + (wide ? 120 : 32);
        int x = ((GuiContainerAccessor) screen).getGuiLeft() + (((GuiContainerAccessor) screen).getXSize() - width) / 2;
        PotionEffect hovered = null;
        int restoreY = ((GuiContainerAccessor) screen).getYSize();
        try {
            ((EmiGuiContainer) screen).setYSize(y);
            for (PotionEffect inst : effects) {
                int ew = wide ? 120 : 32;
                context.push();
                context.resetColor();
                RenderSystem.disableLighting();
                RenderSystem.enableBlend();
                client.getTextureManager().bindTexture(((GuiContainerAccessor) screen).getInventoryResource());
                drawStatusEffectBackgrounds(screen, x, y, wide);
                drawPotionIcon(screen, x, y, inst);
                if (mouseX >= x && mouseX < x + ew && mouseY >= y && mouseY < y + 32) {
                    hovered = inst;
                }
                x += xOff;
                drawStatusEffectDescriptions(screen, x - width, y, inst, wide);
                context.pop();
            }
        } finally {
            ((EmiGuiContainer) screen).setYSize(restoreY);
        }
        if (size > 1) {
            renderTooltip(screen, mouseX, mouseY, hovered);
        }
    }

    private static void renderTooltip(InventoryEffectRenderer screen, int mouseX, int mouseY, PotionEffect effect) {
        if (effect != null) {
            String amplifier = getPotionAmplifier(effect);
            TooltipComponent name = TooltipComponent.of(Text.translatable(effect.getEffectName()).append(Text.literal(amplifier)));
            TooltipComponent duration = TooltipComponent.of(Text.literal(Potion.getDurationString(effect)));
            EmiRenderHelper.drawTooltip(screen, EmiDrawContext.instance(), shim.java.List.of(name, duration), mouseX, Math.max(mouseY, 16));
        }
    }

    private static String getPotionAmplifier(PotionEffect effect) {
        return switch (effect.getAmplifier()) {
            case 1 -> " " + RetroEMI.translate("enchantment.level.2");
            case 2 -> " " + RetroEMI.translate("enchantment.level.3");
            case 3 -> " " + RetroEMI.translate("enchantment.level.4");
            default -> "";
        };
    }

    private static void drawStatusEffectBackgrounds(InventoryEffectRenderer screen, int x, int y, boolean wide) {
        if (wide) {
            screen.drawTexturedModalRect(x, y, 0, 166, 120, 32);
        } else {
            //split so it renders the edge properly
            screen.drawTexturedModalRect(x, y, 0, 166, 28, 32);
            screen.drawTexturedModalRect(x + 28, y, 116, 166, 4, 32);
        }
    }

    private static void drawStatusEffectDescriptions(InventoryEffectRenderer screen, int x, int y, PotionEffect potionEffect, boolean wide) {
        if (wide) {
            String potionName = RetroEMI.translate(potionEffect.getEffectName()) + getPotionAmplifier(potionEffect);
            client.fontRenderer.drawStringWithShadow(potionName, x + 10 + 18, y + 6, 16777215);
            String durationString = Potion.getDurationString(potionEffect);
            client.fontRenderer.drawStringWithShadow(durationString, x + 10 + 18, y + 16, 8355711);
        }
    }

    private static void drawPotionIcon(InventoryEffectRenderer screen, int x, int y, PotionEffect potionEffect) {
        Potion potionType = Potion.potionTypes[potionEffect.getPotionID()];
        if (potionType.hasStatusIcon()) {
            int statusIconIndex = potionType.getStatusIconIndex();
            screen.drawTexturedModalRect(x + 6, y + 7, statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
        }
    }

    private static int changeEffectSpace(InventoryEffectRenderer screen, int original) {
        return switch (EmiConfig.effectLocation) {
            case RIGHT, RIGHT_COMPRESSED, HIDDEN -> ((GuiContainerAccessor) screen).getGuiLeft() + ((GuiContainerAccessor) screen).getXSize() + 2;
            case TOP -> ((GuiContainerAccessor) screen).getGuiLeft();
            case LEFT_COMPRESSED -> ((GuiContainerAccessor) screen).getGuiLeft() - 2 - 32;
            case LEFT -> original;
        };
    }
}
