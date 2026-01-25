package dev.emi.emi.mixin.early.minecraft.client;

import com.rewindmc.retroemi.REMIMixinHooks;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FontRenderer.class, priority = 2000)
public abstract class FontRendererMixin {

    @Shadow protected byte[] glyphWidth;

    @ModifyVariable(
        method = "renderStringAtPos",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;setColor(FFFF)V",
            ordinal = 0,
            shift = At.Shift.AFTER,
            remap = false
        ),
        ordinal = 0 // i
    )
    private int customFontColor(int original, String text, boolean shadow) {
        return REMIMixinHooks.applyCustomFormatCodes((FontRenderer) (Object) this, text, shadow, original);
    }

    // fix vanilla bug
    @Inject(method = "readGlyphSizes", at = @At(value = "INVOKE", target = "Ljava/io/InputStream;read([B)I", shift = At.Shift.AFTER))
    private void fixBrackets(CallbackInfo ci) {
        this.glyphWidth['（'] = 127;
    }
}
