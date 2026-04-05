package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.mixin.accessor.GuiContainerAccessor;
import dev.emi.emi.mixin.accessor.SlotCraftingAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EmiStackProviders {
	public static Map<Class<?>, List<EmiStackProvider<?>>> fromClass = Maps.newHashMap();
	public static List<EmiStackProvider<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static EmiStackInteraction getStackAt(GuiScreen screen, int x, int y, boolean notClick) {
		if (fromClass.containsKey(screen.getClass())) {
			for (EmiStackProvider provider : fromClass.get(screen.getClass())) {
				EmiStackInteraction stack = provider.getStackAt(screen, x, y);
				if (!stack.isEmpty() && (notClick || stack.isClickable())) {
					return stack;
				}
			}
		}
		for (EmiStackProvider handler : generic) {
			EmiStackInteraction stack = handler.getStackAt(screen, x, y);
			if (!stack.isEmpty() && (notClick || stack.isClickable())) {
				return stack;
			}
		}
		if (notClick && screen instanceof GuiContainerAccessor handled) {
			Slot s = handled.getTheSlot();
			if (s != null) {
				ItemStack stack = s.getStack();
				if (!stack.isEmpty()) {
					if (s instanceof SlotCrafting craf) {
						// Emi be making assumptions
						try {
							InventoryCrafting inv = ((SlotCraftingAccessor) craf).getCraftMatrix();
							Minecraft client = Minecraft.getMinecraft();
							for (IRecipe r : ForgeRegistries.RECIPES.getValuesCollection()) {
								if (r.matches(inv, client.world)) {
                                    ResourceLocation id = EmiPort.getId(r);
									EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
									if (recipe != null) {
										return new EmiStackInteraction(EmiStack.of(stack), recipe, false);
									}
								}
							}
						} catch (Exception e) {
						}
					}
					return new EmiStackInteraction(EmiStack.of(stack));
				}
			}
		}
		return EmiStackInteraction.EMPTY;
	}
}
