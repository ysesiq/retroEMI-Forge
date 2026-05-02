package dev.emi.emi.mixin.accessor;

import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderItem.class)
public interface RenderItemAccessor {

	@Invoker("renderModel")
	void invokeRenderBakedItemModel(IBakedModel bakedmodel, ItemStack stack);
}
