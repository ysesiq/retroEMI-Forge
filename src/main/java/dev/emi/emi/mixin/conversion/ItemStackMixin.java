package dev.emi.emi.mixin.conversion;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackConvertible;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class ItemStackMixin implements EmiStackConvertible {
    @Override
    public EmiStack emi() {
        return EmiStack.of((ItemStack) (Object) this);
    }

    @Override
    public EmiStack emi(long amount) {
        return EmiStack.of((ItemStack) (Object) this, amount);
    }
}
