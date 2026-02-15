package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.handler.CoercedRecipeHandler;
import dev.emi.emi.mixin.accessor.SlotCraftingAccessor;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiSidebars;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import com.rewindmc.retroemi.ItemStacks;
import com.rewindmc.retroemi.RetroEMI;

public class EmiRecipeFiller {
	public static Map<Class<? extends Container>, List<EmiRecipeHandler<?>>> handlers = Maps.newHashMap();
	public static BiFunction<Container, EmiRecipe, EmiRecipeHandler<?>> extraHandlers = (h, r) -> null;

	public static void clear() {
		handlers.clear();
		extraHandlers = (h, r) -> null;
	}

	public static boolean isSupported(EmiRecipe recipe) {
		for (List<EmiRecipeHandler<?>> list : handlers.values()) {
			for (EmiRecipeHandler<?> handler : list) {
				if (handler.supportsRecipe(recipe) && handler.alwaysDisplaySupport(recipe)) {
					return true;
				}
			}
		}
		GuiContainer hs = EmiApi.getHandledScreen();
		if (hs != null) {
			for (EmiRecipeHandler<?> handler : getAllHandlers(hs)) {
				if (handler.supportsRecipe(recipe)) {
					return true;
				}
			}
			EmiRecipeHandler<?> handler = extraHandlers.apply(hs.inventorySlots, recipe);
			if (handler != null && handler.supportsRecipe(recipe)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Container> List<EmiRecipeHandler<T>> getAllHandlers(GuiContainer screen) {
		if (screen != null) {
			T screenHandler = (T) screen.inventorySlots;
			Class<? extends Container> type;
			try {
				type = (screenHandler instanceof ContainerPlayer) ? null : screenHandler.getClass();
			} catch (UnsupportedOperationException e) {
				type = null;
			}
			if ((type != null || screenHandler instanceof ContainerPlayer) && handlers.containsKey(type)) {
				return (List<EmiRecipeHandler<T>>) (List<?>) handlers.get(type);
			}
			for (Slot slot : (List<Slot>) screen.inventorySlots.inventorySlots) {
				if (slot instanceof SlotCrafting crs) {
					var inv = ((SlotCraftingAccessor) crs).getCraftMatrix();
					if (inv != null && inv.getSizeInventory() > 0) {
						return shim.java.List.of(new CoercedRecipeHandler<T>(crs));
					}
				}
			}
		}
		return shim.java.List.of();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Container> @Nullable EmiRecipeHandler<T> getFirstValidHandler(EmiRecipe recipe, GuiContainer screen) {
		EmiRecipeHandler<T> ret = null;
		for (EmiRecipeHandler<T> handler : EmiRecipeFiller.<T>getAllHandlers(screen)) {
			if (handler.supportsRecipe(recipe)) {
				ret = handler;
				break;
			}
		}
		if (ret == null || (ret instanceof CoercedRecipeHandler && !(screen instanceof GuiInventory))) {
			EmiRecipeHandler<T> extra = (EmiRecipeHandler<T>) extraHandlers.apply(screen.inventorySlots, recipe);
			if (extra != null) {
				ret = extra;
			}
		}
		return ret;
	}

	public static <T extends Container> boolean performFill(EmiRecipe recipe, GuiContainer screen,
			EmiCraftContext.Type type, EmiCraftContext.Destination destination, int amount) {
		EmiRecipeHandler<T> handler = getFirstValidHandler(recipe, screen);
		if (handler != null && handler.supportsRecipe(recipe)) {
			EmiPlayerInventory inv = handler.getInventory(screen);
			EmiCraftContext<T> context = new EmiCraftContext<T>(screen, inv, type, destination, amount);
			if (handler.canCraft(recipe, context)) {
				EmiSidebars.craft(recipe);
				boolean crafted = handler.craft(recipe, context);
				if (crafted) {
					Minecraft.getMinecraft().displayGuiScreen(screen);
				}
				return crafted;
			}
		}
		return false;
	}

	public static <T extends Container> @Nullable List<ItemStack> getStacks(StandardRecipeHandler<T> handler, EmiRecipe recipe, GuiContainer screen, int amount) {
		try {
			T screenHandler = (T) screen.inventorySlots;
			if (handler != null) {
				List<Slot> slots = handler.getInputSources(screenHandler);
				List<Slot> craftingSlots = handler.getCraftingSlots(recipe, screenHandler);
				List<EmiIngredient> ingredients = recipe.getInputs();
				List<DiscoveredItem> discovered = Lists.newArrayList();
				Object2IntMap<EmiStack> weightDivider = new Object2IntOpenHashMap<>();
				for (int i = 0; i < ingredients.size(); i++) {
					List<DiscoveredItem> d = Lists.newArrayList();
					EmiIngredient ingredient = ingredients.get(i);
					List<EmiStack> emiStacks = ingredient.getEmiStacks();
					if (ingredient.isEmpty()) {
						discovered.add(null);
						continue;
					}
					for (int e = 0; e < emiStacks.size(); e++) {
						EmiStack stack = emiStacks.get(e);
						slotLoop:
						for (Slot s : slots) {
							ItemStack ss = s.getStack();
							if (EmiStack.of(s.getStack()).isEqual(stack)) {
								for (DiscoveredItem di : d) {
									if (RetroEMI.canCombine(ss, di.stack)) {
										di.amount += ss.stackSize;
										continue slotLoop;
									}
								}
								d.add(new DiscoveredItem(stack, ss, ss.stackSize, (int) ingredient.getAmount(), ss.getMaxStackSize()));
							}
						}
					}
					DiscoveredItem biggest = null;
					for (DiscoveredItem di : d) {
						if (biggest == null) {
							biggest = di;
						} else {
							int a = di.amount / (weightDivider.getOrDefault(di.ingredient, 0) + di.consumed);
							int ba = biggest.amount / (weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
							if (ba < a) {
								biggest = di;
							}
						}
					}
					if (biggest == null || i >= craftingSlots.size()) {
						return null;
					}
					Slot slot = craftingSlots.get(i);
					if (recipe instanceof EmiCraftingRecipe craft && craft.shapeless && i > 1 && screen instanceof GuiInventory) {
						slot = craftingSlots.get(i + 1);
					}
					if (slot == null) {
						return null;
					}
					weightDivider.put(biggest.ingredient, weightDivider.getOrDefault(biggest.ingredient, 0) + biggest.consumed);
					biggest.max = Math.min(biggest.max, 64 /*slot.getStack().getMaxStackSize()*/);
					discovered.add(biggest);
				}
				if (discovered.isEmpty()) {
					return null;
				}

				List<DiscoveredItem> unique = Lists.newArrayList();
				outer:
				for (DiscoveredItem di : discovered) {
					if (di == null) {
						continue;
					}
					for (DiscoveredItem ui : unique) {
						if (RetroEMI.canCombine(di.stack, ui.stack)) {
							ui.consumed += di.consumed;
							continue outer;
						}
					}
					unique.add(new DiscoveredItem(di.ingredient, di.stack, di.amount, di.consumed, di.max));
				}
				int maxAmount = Integer.MAX_VALUE;
				for (DiscoveredItem ui : unique) {
					if (!ui.catalyst()) {
						maxAmount = Math.min(maxAmount, ui.amount / ui.consumed);
						maxAmount = Math.min(maxAmount, ui.max);
					}
				}
				maxAmount = Math.min(maxAmount, amount + batchesAlreadyPresent(recipe, handler, screen));

				if (maxAmount == 0) {
					return null;
				}

				List<ItemStack> desired = Lists.newArrayList();
				for (int i = 0; i < discovered.size(); i++) {
					DiscoveredItem di = discovered.get(i);
					if (di != null) {
						ItemStack is = di.stack.copy();
						int a = di.catalyst() ? di.consumed : di.consumed * maxAmount;
						is.stackSize = (a);
						desired.add(is);
					} else {
						desired.add(ItemStacks.EMPTY);
					}
				}
				return desired;
			}
		} catch (Exception e) {
			EmiLog.error("Error collecting stacks", e);
		}
		return null;
	}

	public static <T extends Container> int batchesAlreadyPresent(EmiRecipe recipe, StandardRecipeHandler<T> handler, GuiContainer screen) {
		List<EmiIngredient> inputs = recipe.getInputs();
		List<ItemStack> stacks = Lists.newArrayList();
		Slot output = handler.getOutputSlot((T)screen.inventorySlots);
		if (output != null && !ItemStacks.isEmpty(output.getStack()) && recipe.getOutputs().size() > 0
				&& !RetroEMI.canCombine(output.getStack(), recipe.getOutputs().get(0).getItemStack())) {
			return 0;
		}
		for (Slot slot : handler.getCraftingSlots(recipe, (T)screen.inventorySlots)) {
			if (slot != null) {
				stacks.add(slot.getStack());
			} else {
				stacks.add(ItemStacks.EMPTY);
			}
		}
		long amount = Long.MAX_VALUE;
		outer:
		for (int i = 0; i < inputs.size(); i++) {
			EmiIngredient input = inputs.get(i);
			if (input.isEmpty()) {
				if (ItemStacks.isEmpty(stacks.get(i))) {
					continue;
				}
				return 0;
			}
			if (i >= stacks.size()) {
				return 0;
			}
			EmiStack es = EmiStack.of(stacks.get(i));
			for (EmiStack v : input.getEmiStacks()) {
				if (v.isEmpty()) {
					continue;
				}
				if (v.isEqual(es) && es.getAmount() >= v.getAmount()) {
					amount = Math.min(amount, es.getAmount() / v.getAmount());
					continue outer;
				}
			}
			return 0;
		}
		if (amount < Long.MAX_VALUE && amount > 0) {
			return (int) amount;
		}
		return 0;
	}

	public static <T extends Container> boolean clientFill(StandardRecipeHandler<T> handler, EmiRecipe recipe,
		GuiContainer screen, List<ItemStack> stacks, EmiCraftContext.Destination destination) {
		Minecraft client = Minecraft.getMinecraft();
		T screenHandler = (T) screen.inventorySlots;
		if (handler != null && client.thePlayer.inventory.getItemStack() == null) {
			PlayerControllerMP manager = client.playerController;
			EntityPlayer player = client.thePlayer;
			List<Slot> clear = handler.getCraftingSlots(screenHandler);
			for (Slot slot : clear) {
				if (slot != null) {
					manager.windowClick(screenHandler.windowId, slot.slotNumber, 0, 1, player);
				}
			}
			List<Slot> inputs = handler.getInputSources(screenHandler);
			List<Slot> slots = handler.getCraftingSlots(recipe, screenHandler);
			outer:
			for (int i = 0; i < stacks.size(); i++) {
				ItemStack stack = stacks.get(i);
				if (ItemStacks.isEmpty(stack)) {
					continue;
				}
				if (i >= slots.size()) {
					return false;
				}
				Slot crafting = slots.get(i);
				if (recipe instanceof EmiCraftingRecipe craf && craf.shapeless && i > 1 && screen instanceof GuiInventory) {
					crafting = slots.get(i + 1);
				}
				if (crafting == null) {
					return false;
				}
				int needed = stack.stackSize;
				for (Slot input : inputs) {
					if (slots.contains(input) || input.getStack() == null) {
						continue;
					}
					ItemStack is = input.getStack().copy();
					if (RetroEMI.canCombine(is, stack)) {
						manager.windowClick(screenHandler.windowId, input.slotNumber, 0, 0, player);
						if (is.stackSize <= needed) {
							needed -= is.stackSize;
							manager.windowClick(screenHandler.windowId, crafting.slotNumber, 0, 0, player);
						} else {
							while (needed > 0) {
								manager.windowClick(screenHandler.windowId, crafting.slotNumber, 1, 0, player);
								needed--;
							}
							manager.windowClick(screenHandler.windowId, input.slotNumber, 0, 0, player);
						}
					}
					if (needed == 0) {
						continue outer;
					}
				}
				return false;
			}
			Slot slot = handler.getOutputSlot(screenHandler);
			if (slot != null) {
				if (destination == EmiCraftContext.Destination.CURSOR) {
					manager.windowClick(screenHandler.windowId, slot.slotNumber, 0, 0, player);
				} else if (destination == EmiCraftContext.Destination.INVENTORY) {
					manager.windowClick(screenHandler.windowId, slot.slotNumber, 0, 1, player);
				}
			}
			return true;
		}
		return false;
	}

	private static class DiscoveredItem {
		private static final Comparison COMPARISON = Comparison.DEFAULT_COMPARISON;
		public EmiStack ingredient;
		public ItemStack stack;
		public int consumed;
		public int amount;
		public int max;

		public DiscoveredItem(EmiStack ingredient, ItemStack stack, int amount, int consumed, int max) {
			this.ingredient = ingredient;
			this.stack = stack.copy();
			this.amount = amount;
			this.consumed = consumed;
			this.max = max;
		}

		public boolean catalyst() {
			return ingredient.getRemainder().isEqual(ingredient, COMPARISON);
		}
	}
}
