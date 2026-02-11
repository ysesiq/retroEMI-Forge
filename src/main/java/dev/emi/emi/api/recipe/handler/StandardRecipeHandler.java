package dev.emi.emi.api.recipe.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface StandardRecipeHandler<T extends Container> extends EmiRecipeHandler<T> {

	/**
	 * @return The slots for the recipe handler to source ingredients from.
	 * Typically this should include the player's inventory, and crafting slots.
	 */
	List<Slot> getInputSources(T handler);

	/**
	 * @return The slots where inputs should be placed to perform crafting.
	 */
	List<Slot> getCraftingSlots(T handler);

	/**
	 * @return The slots where inputs should be placed to perform crafting for a particular context.
	 */
	default List<Slot> getCraftingSlots(EmiRecipe recipe, T handler) {
		return getCraftingSlots(handler);
	}

	/**
	 * @return The output slot for recipe handlers that support instant interaction, like crafting tables.
	 * For handlers that have processing time, or where this concept is otherwise inapplicable, null.
	 */
	default @Nullable Slot getOutputSlot(T handler) {
		return null;
	}

	@Override
	default EmiPlayerInventory getInventory(GuiContainer screen) {
		return new EmiPlayerInventory(getInputSources((T) screen.inventorySlots).stream().map(Slot::getStack).map(EmiStack::of).collect(Collectors.toList()));
	}

	@Override
	default boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
		return context.getInventory().canCraft(recipe);
	}

	@Override
	default boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {

		List<ItemStack> stacks = EmiRecipeFiller.getStacks(this, recipe, context.getScreen(), context.getAmount());
		if (stacks != null) {
			Minecraft.getMinecraft().displayGuiScreen(context.getScreen());
			if (!EmiClient.onServer) {
				return EmiRecipeFiller.clientFill(this, recipe, context.getScreen(), stacks, context.getDestination());
			} else {
				EmiClient.sendFillRecipe(this, context.getScreen(), context.getScreenHandler().windowId, switch (context.getDestination()) {
					case NONE -> 0;
					case CURSOR -> 1;
					case INVENTORY -> 2;
				}, stacks, recipe);
			}
			return true;
		}
		return false;
	}

	@Override
	default void render(EmiRecipe recipe, EmiCraftContext<T> context, List<Widget> widgets, DrawContext draw) {
		renderMissing(recipe, context.getInventory(), widgets, draw);
	}

	@ApiStatus.Internal
	public static void renderMissing(EmiRecipe recipe, EmiPlayerInventory inv, List<Widget> widgets, DrawContext draw) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		context.enableDepthTest();
		Map<EmiIngredient, Boolean> availableForCrafting = getAvailable(recipe, inv);
		for (Widget w : widgets) {
			if (w instanceof SlotWidget sw) {
				EmiIngredient stack = sw.getStack();
				Bounds bounds = sw.getBounds();
				if (sw.getRecipe() == null && availableForCrafting.containsKey(stack) && !stack.isEmpty()) {
					if (availableForCrafting.get(stack)) {
						//context.fill(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0x4400FF00);
					} else {
						context.fill(bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0x44FF0000);
					}
				}
			}
		}
		context.resetColor();
	}

    static Map<EmiIngredient, Boolean> getAvailable(EmiRecipe recipe, EmiPlayerInventory inventory) {
		Map<EmiIngredient, Boolean> availableForCrafting = new IdentityHashMap<>();
		List<Boolean> list = inventory.getCraftAvailability(recipe);
		List<EmiIngredient> inputs = recipe.getInputs();
		if (list.size() != inputs.size()) {
			return Collections.emptyMap();
		}
		for (int i = 0; i < list.size(); i++) {
			availableForCrafting.put(inputs.get(i), list.get(i));
		}
		return availableForCrafting;
	}
}
