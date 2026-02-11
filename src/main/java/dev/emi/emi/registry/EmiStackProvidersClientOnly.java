package dev.emi.emi.registry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.mixin.accessor.SlotCraftingAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SideOnly(Side.CLIENT)
public class EmiStackProvidersClientOnly {
	@Nullable
	public static EmiStackInteraction getEmiStackInteraction(Slot s, ItemStack stack) {
		if (s instanceof SlotCrafting craf) {
			// Emi be making assumptions
			try {
				InventoryCrafting inv = (InventoryCrafting) ((SlotCraftingAccessor) craf).getCraftMatrix();
				Minecraft client = Minecraft.getMinecraft();
				List<IRecipe> list = CraftingManager.getInstance().getRecipeList();
				for (var r : list) {
					if (r.matches(inv, client.theWorld)) {
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
		return null;
	}
}
