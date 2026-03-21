package dev.emi.emi.mixin.accessor;

import net.minecraft.item.Item;
import net.minecraft.item.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemTool.class)
public interface ItemToolAccessor {
    @Accessor("toolMaterial")
    Item.ToolMaterial getToolMaterial();
}
