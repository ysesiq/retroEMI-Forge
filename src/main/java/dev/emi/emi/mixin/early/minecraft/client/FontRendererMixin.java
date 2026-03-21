package dev.emi.emi.mixin.early.minecraft.client;

import com.rewindmc.retroemi.REMIMixinHooks;
import dev.emi.emi.mixinsupport.inject_interface.EmiFontRenderer;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FontRenderer.class, priority = 2000)
public abstract class FontRendererMixin implements EmiFontRenderer {

    @Shadow private int textColor;

    @ModifyVariable(
        method = "renderStringAtPos",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;setColor(FFFF)V",
            ordinal = 0,
            shift = At.Shift.AFTER
        ),
        ordinal = 0 // i
    )
    private int customFontColor(int original, String text, boolean shadow) {
        return REMIMixinHooks.applyCustomFormatCodes((FontRenderer) (Object) this, text, shadow, original);
    }

    @Override
    public int setTextColor(int color) {
        return this.textColor = color;
    }
}
