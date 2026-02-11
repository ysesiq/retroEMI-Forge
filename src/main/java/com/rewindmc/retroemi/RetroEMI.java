package com.rewindmc.retroemi;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import shim.com.mojang.blaze3d.systems.RenderSystem;
import cpw.mods.fml.common.registry.GameData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.FMLCommonHandler;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringTranslate;
import shim.net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TooltipPositioner;
import shim.net.minecraft.client.util.math.Vec2i;

public class RetroEMI {
	public static final RetroEMI instance = new RetroEMI();
	public final RenderItem itemRenderer;

	private static final List<Runnable> tickQueue = new ArrayList<>();

	private RetroEMI() {
		if (!FMLCommonHandler.instance().getSide().isServer()) {
			itemRenderer = new RenderItem();
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

	public static void executeOnMainThread(Runnable r) {
		synchronized (tickQueue) {
			tickQueue.add(r);
		}
	}

	public static void tick() {
		Runnable[] queue;
		synchronized (tickQueue) {
			queue = tickQueue.toArray(new Runnable[tickQueue.size()]);
			tickQueue.clear();
		}
		for (Runnable r : queue) {
			r.run();
		}
	}

	public static Collection<PotionEffect> getEffects(EmiStack stack) {
		if (stack.getItemStack().getItem() instanceof ItemPotion p) {
			return p.getEffects(stack.getItemStack());
		}
		return Collections.emptyList();
	}

	public static List<String> wrapLines(String str, int cols) {
		ArrayList<String> li = new ArrayList<String>();
		StringBuilder buf = new StringBuilder();
		for (String line : str.split("\n")) {
			int w = -1;
			for (String word : line.split(" ")) {
				if (w + 1 + word.length() > cols) {
					li.add(buf.toString());
					buf.setLength(0);
					w = 0;
				} else {
					if (w != -1) buf.append(" ");
					w++;
				}
				while (word.length() > cols) {
					li.add(word.substring(0, cols));
					word = word.substring(cols);
				}
				buf.append(word);
				w += word.length();
			}
			if (buf.length() > 0) {
				li.add(buf.toString());
			}
			buf.setLength(0);
		}
		return li;
	}

	public static String join(List<String> strs, String delim) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : strs) {
			if (first) {
				first = false;
			} else {
				sb.append(delim);
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public static void offerOrDrop(EntityPlayer player, ItemStack stack) {
		if (!player.inventory.addItemStackToInventory(stack)) {
			player.dropPlayerItemWithRandomChoice(stack, false);
		}
	}

	public static boolean canCombine(ItemStack a, ItemStack b) {
		return a == null || b == null ? a == b : a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
	}

	public static void renderModernTooltip(GuiScreen screen, List<TooltipComponent> components, int x, int y, int maxWidth, TooltipPositioner positioner) {
		FontRenderer textRenderer = Minecraft.getMinecraft().fontRenderer;
		TooltipComponent tooltipComponent2;
		int r;
		if (components.isEmpty()) {
			return;
		}
		int i = 0;
		int j = components.size() == 1 ? -2 : 0;
		for (TooltipComponent tooltipComponent : components) {
			int k = tooltipComponent.getWidth(textRenderer);
			if (k > i) {
				i = k;
			}
			j += tooltipComponent.getHeight();
		}
		int l = i;
		int m = j;
		Vec2i vector2ic = positioner.getPosition(screen, x, y, l, m);
		int n = vector2ic.x();
		int o = vector2ic.y();
		glPushMatrix();
		int p = 400;
		Tessellator tess = Tessellator.instance;
		glDisable(GL_TEXTURE_2D);
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		EmiDrawContext.instance().context.zLevel += 300;
		TooltipBackgroundRenderer.render(
				(builder, startX, startY, endX, endY, z, colorStart, colorEnd) -> EmiDrawContext.instance().context.drawGradientRect(startX, startY, endX, endY,
						colorStart, colorEnd), tess, n, o, l, m, 400);
		EmiDrawContext.instance().context.zLevel -= 300;
		glTranslatef(0.0f, 0.0f, 400.0f);
		int q = o;
		for (r = 0; r < components.size(); ++r) {
			tooltipComponent2 = components.get(r);
			tooltipComponent2.drawText(textRenderer, n, q);
			q += tooltipComponent2.getHeight() + (r == 0 ? 2 : 0);
		}
		q = o;
		for (r = 0; r < components.size(); ++r) {
			tooltipComponent2 = components.get(r);
			tooltipComponent2.drawItems(textRenderer, n, q);
			q += tooltipComponent2.getHeight() + (r == 0 ? 2 : 0);
		}
		glPopMatrix();
	}

	public static final IntSet heldButtons = new IntOpenHashSet();

	public static boolean handleMouseInput() {
		try {
			Minecraft client = Minecraft.getMinecraft();
			GuiScreen screen = client.currentScreen;
			if (screen instanceof GuiContainer hs) {
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
					if (EmiScreenManager.mouseScrolled(mx, my, dwheel / 120D)) {
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

//	public static EmiIngredient wildcardIngredient(ItemStack stack) {
//		if (stack != null && stack.getItemDamage() == 32767) {
//			EmiIngredient item = EmiIngredient.of(new WildcardItemTag(stack.getItem()));
//			if (item.getEmiStacks().size() == 1) {
//				return item;
//			} else {
//				return EmiIngredient.of(com.rewindmc.retroemi.shim.java.List.of(item), 65); // Stack size of 1
//			}
//		}
//		return EmiStack.of(stack, 1);
//	}
//
//	public static EmiIngredient wildcardIngredientWithStackSize(ItemStack stack) {
//		if (stack != null && stack.getItemDamage() == 32767) {
//			EmiIngredient item = EmiIngredient.of(new WildcardItemTag(stack.getItem()));
//			if (item.getEmiStacks().size() == 1) {
//				return item;
//			} else {
//				return EmiIngredient.of(com.rewindmc.retroemi.shim.java.List.of(item), stack.stackSize + 64);
//			}
//		}
//		return EmiStack.of(stack);
//	}

	public static String compactify(ResourceLocation id) {
		// 1.4 limits channel length to 20 chars, but all strings in the protocol are UTF-16
		// so this doesn't cost anything extra - same trick used by UpsilonFixes

		// Is this necessary? -Bagel
		return id.toString().replace("emi:", "ē").replace("_", "");
	}

	public static String translate(String s) {
		return StringTranslate.getInstance().translateKey(s);
	}

	public static String translate(String s, Object... arg) {
		return StringTranslate.getInstance().translateKeyFormat(s, arg);
	}

    public static String sanitizeNBT(String nbt) {
        nbt = nbt.replace(" ", "");
        if (!nbt.startsWith("{")) {
            nbt = "{" + nbt;
        }
        if (!nbt.endsWith("}")) {
            nbt = nbt + "}";
        }
        return nbt;
    }

    public static String replaceCharAt(String s, int index, char c) {
        return s.substring(0, index) + c + s.substring(index + 1);
    }

    public static List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        ((Iterable<Item>) GameData.getItemRegistry()).forEach(items::add);
        return items;
    }

    public static int getScaledHeight(Minecraft client) {
        return client.displayHeight / EmiPort.getGuiScale(client);
    }

    public static int getScaledWidth(Minecraft client) {
        return client.displayWidth / EmiPort.getGuiScale(client);
    }
}
