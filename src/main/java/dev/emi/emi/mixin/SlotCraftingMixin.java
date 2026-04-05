package dev.emi.emi.mixin;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.runtime.EmiSidebars;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlotCrafting.class)
public class SlotCraftingMixin {
	@Shadow @Final
	private InventoryCrafting craftMatrix;
	@Shadow @Final
	private EntityPlayer player;

	@Inject(at = @At("HEAD"), method = "onCrafting(Lnet/minecraft/item/ItemStack;)V")
	private void onCrafted(ItemStack stack, CallbackInfo info) {
		World world = player.getEntityWorld();
		if (world.isRemote) {
			for (IRecipe r : ForgeRegistries.RECIPES.getValuesCollection()) {
				if (r.matches(craftMatrix, world)) {
					EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(EmiPort.getId(r));
					if (recipe != null) {
						EmiSidebars.craft(recipe);
						return;
					}
				}
			}
		}
	}
}
