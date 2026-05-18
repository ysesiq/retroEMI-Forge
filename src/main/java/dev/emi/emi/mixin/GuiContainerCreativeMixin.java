package dev.emi.emi.mixin;

import com.rewindmc.retroemi.RetroEMI;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainerCreative.class)
public class GuiContainerCreativeMixin {
    @Inject(method = "handleMouseInput",
        at = @At("HEAD"),
        cancellable = true
    )
    public void handleMouseInput(CallbackInfo ci) {
        if (RetroEMI.handleMouseInput()) {
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At(value = "HEAD"), cancellable = true)
    public void blockEMISearchToCreativeSearch(CallbackInfo ci) {
        if (RetroEMI.handleKeyboardInput()) {
            ci.cancel();
        }
    }
}
