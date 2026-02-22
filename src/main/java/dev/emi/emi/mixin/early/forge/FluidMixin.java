package dev.emi.emi.mixin.early.forge;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackConvertible;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraftforge.fluids.Fluid;

@Mixin(Fluid.class)
public class FluidMixin implements EmiStackConvertible {
    @Override
    public EmiStack emi() {
        return EmiStack.of((Fluid) (Object) this);
    }

    @Override
    public EmiStack emi(long amount) {
        return EmiStack.of((Fluid) (Object) this, amount);
    }
}
