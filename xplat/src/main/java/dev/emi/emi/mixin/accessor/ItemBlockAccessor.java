package dev.emi.emi.mixin.accessor;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemBlock.class)
public interface ItemBlockAccessor {
	@Accessor("field_150939_a")
	Block getBlock();
}
