package shim.net.minecraft.registry.tag;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Desugar
public record ItemKey(Item item, int meta) {

    public static ItemKey of(ItemStack stack) {
        return new ItemKey(stack.getItem(), stack.getItemDamage());
    }

    public ItemStack toStack() {
        return new ItemStack(item, 1, meta);
    }

}
