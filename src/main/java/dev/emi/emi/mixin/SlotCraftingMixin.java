package dev.emi.emi.mixin;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiSidebars;
import dev.emi.emi.runtime.ProxyRecipeManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotCrafting.class)
public class SlotCraftingMixin {
	@Shadow @Final
	private IInventory craftMatrix;
	@Shadow @Final
	private EntityPlayer thePlayer;

	@Inject(at = @At("HEAD"), method = "onCrafting(Lnet/minecraft/item/ItemStack;)V")
	private void onCrafted(ItemStack stack, CallbackInfo info) {
		World world = thePlayer.getEntityWorld();
		if (world.isRemote) {
			IRecipe crafting = ProxyRecipeManager.getFirst((InventoryCrafting) craftMatrix);
			if (crafting != null) {
				EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(ProxyRecipeManager.getId(crafting));
				if (recipe != null) {
					EmiSidebars.craft(recipe);
				}
			}
		}
	}
}
