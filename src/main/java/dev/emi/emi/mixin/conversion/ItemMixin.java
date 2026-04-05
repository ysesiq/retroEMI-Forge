package dev.emi.emi.mixin.conversion;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackConvertible;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Items.class)
public class ItemMixin implements EmiStackConvertible {
    @Override
    public EmiStack emi() {
        return EmiStack.of((Item) (Object) this);
    }

    @Override
    public EmiStack emi(long amount) {
        return EmiStack.of((Item) (Object) this, amount);
    }
}
