package dev.emi.emi.platform;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.FillRecipeC2SPacket;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class EmiClient {
	public static boolean onServer = false;

	public static void init() {
		EmiConfig.loadConfig();
	}

	public static <T extends Container> void sendFillRecipe(StandardRecipeHandler<T> handler, GuiContainer screen,
			int syncId, int action, List<ItemStack> stacks, EmiRecipe recipe) {
		T screenHandler = (T)screen.inventorySlots;
		List<Slot> crafting = handler.getCraftingSlots(recipe, screenHandler);
		Slot output = handler.getOutputSlot(screenHandler);
		EmiNetwork.sendToServer(new FillRecipeC2SPacket(screenHandler, action, handler.getInputSources(screenHandler), crafting, output, stacks));
	}
}
