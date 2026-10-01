package com.rewindmc.retroemi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.gtnewhorizon.gtnhlib.client.event.RenderTooltipEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import dev.emi.emi.mixin.accessor.GuiTextFieldAccessor;
import dev.emi.emi.platform.EmiAgnos;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import shim.net.minecraft.client.gui.ParentElement;
import shim.net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TextTooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TooltipPositioner;
import shim.net.minecraft.item.ItemStacks;
import shim.net.minecraft.client.renderer.GlStateManager;
import shim.net.minecraft.client.util.ITooltipFlag;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.client.util.math.Vec2i;
import shim.net.minecraft.text.MutableText;
import shim.net.minecraft.text.Text;
import shim.net.minecraft.util.Formatting;

public class RetroEMI {
	public static final RetroEMI instance = new RetroEMI();
	public final RenderItem itemRenderer;

	private RetroEMI() {
		if (!FMLCommonHandler.instance().getSide().isServer()) {
			itemRenderer = RenderItem.getInstance();
		} else {
			itemRenderer = null;
		}
	}

	public static boolean isSideLit(ItemStack item) {
		if (item.getItem() instanceof ItemBlock) {
			Block b = ((ItemBlock) item.getItem()).field_150939_a;
			return RenderBlocks.renderItemIn3d(b.getRenderType());
		}
		return false;
	}

	public static Collection<PotionEffect> getEffects(EmiStack stack) {
		if (stack.getItemStack().getItem() instanceof ItemPotion p) {
			return p.getEffects(stack.getItemStack());
		}
		return Collections.emptyList();
	}

	public static final IntSet heldButtons = new IntOpenHashSet();

	public static boolean handleMouseInput() {
		try {
			Minecraft client = Minecraft.getMinecraft();
			GuiScreen screen = client.currentScreen;
			if (screen instanceof GuiContainer) {
				ScaledResolution sr = new ScaledResolution(client, client.displayWidth, client.displayHeight);
				double xScale = (sr.getScaledWidth_double() / client.displayWidth);
				double yScale = (sr.getScaledHeight_double() / client.displayHeight);
				double mx = Mouse.getEventX() * xScale;
				double my = (client.displayHeight - Mouse.getEventY()) * yScale;
				int button = Mouse.getEventButton();
				if (button == -1) {
					for (Integer btn : heldButtons) {
						if (EmiScreenManager.mouseDragged(mx, my, btn, Mouse.getDX() * xScale, Mouse.getDY() * -yScale)) {
							return true;
						}
					}
				} else {
					if (Mouse.getEventButtonState()) {
						heldButtons.add(button);
						if (EmiScreenManager.mouseClicked(mx, my, button)) {
							return true;
						}
					} else {
						heldButtons.remove(button);
						if (EmiScreenManager.mouseReleased(mx, my, button)) {
							return true;
						}
					}
				}
				int dwheel = Mouse.getEventDWheel();
				if (dwheel != 0) {
					double factor = 1D;
					if (!EmiAgnos.isModLoaded("lwjgl3ify")) factor = 120D;
					if (EmiScreenManager.mouseScrolled(mx, my, dwheel / factor)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling mouse event", e);
		}
		return false;
	}

	public static boolean handleKeyboardInput() {
		try {
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			int k = Keyboard.getEventKey();
			char c = Keyboard.getEventCharacter();
			if (screen instanceof GuiContainer) {
				if (Keyboard.getEventKeyState() || k == 0 && Character.isDefined(c)) {
					if (Keyboard.getEventCharacter() != 0 && !Character.isISOControl(Keyboard.getEventCharacter())) {
						if (EmiScreenManager.search.charTyped(Keyboard.getEventCharacter(), EmiInput.getCurrentModifiers())) {
							return true;
						}
					}
					if (EmiScreenManager.keyPressed(Keyboard.getEventKey() , 0, EmiInput.getCurrentModifiers())) {
						return true;
					}
				} else {
//					if (EmiScreenManager.keyReleased(Keyboard.getEventKey(), 0, EmiInput.getCurrentModifiers())) {
//						return true;
//					}
				}
			}
		} catch (Exception e) {
			EmiLog.error("Error while handling key press", e);
		}
		return false;
	}

	public static String translate(String s) {
		return I18n.format(s);
	}

	public static String translate(String s, Object... arg) {
		return I18n.format(s, arg);
	}

	public static boolean hasTranslation(String s) {
		return !I18n.format(s).equals(s);
	}

	public static List<Item> getAllItems() {
		List<Item> items = new ArrayList<>();
		((Iterable<Item>) EmiPort.getItemRegistry()).forEach(items::add);
		return items;
	}

	public static int getScaledHeight(Minecraft client) {
		return client.displayHeight / EmiPort.getGuiScale(client);
	}

	public static int getScaledWidth(Minecraft client) {
		return client.displayWidth / EmiPort.getGuiScale(client);
	}

	public static List<Text> getItemToolTip(ItemStack stack, ITooltipFlag.TooltipFlags type) {
		try {
			List<String> rawTip = stack.getTooltip(Minecraft.getMinecraft().thePlayer, type.isAdvanced());
			List<Text> tip = rawTip.stream().map(Text::literal).map(t -> t.formatted(Formatting.GRAY)).collect(Collectors.toList());
			if (!tip.isEmpty()) {
				tip.set(0, ((MutableText) tip.get(0)).formatted(Formatting.byName(stack.getItem().getRarity(stack).rarityColor.name())));
			}
			return tip;
		} catch (Throwable e) {
			EmiLog.error("Error getting tooltip for " + stack, e);
			return shim.java.List.of(EmiStack.of(stack).getName());
		}
	}

	public static boolean hasFocusedTextReflectField(Object parent) {
		// Haha, I'm in danger
		if (parent instanceof ParentElement) {
			return false;
		}
		for (Field f : parent.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if (!GuiTextField.class.isAssignableFrom(f.getType())) {
				continue;
			}
			try {
				if (f.get(parent) instanceof GuiTextField wtf) {
					if (((GuiTextFieldAccessor) wtf).isEnabled() && wtf.isFocused()) {
						return true;
					}
				}
			} catch (Throwable e) {
			}
		}
		return false;
	}
}
