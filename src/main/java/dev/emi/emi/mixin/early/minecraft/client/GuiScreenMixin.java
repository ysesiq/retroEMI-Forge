package dev.emi.emi.mixin.early.minecraft.client;

import dev.emi.emi.mixinsupport.inject_interface.EmiSearchInput;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.rewindmc.retroemi.RetroEMI;

@Mixin(GuiScreen.class)
public class GuiScreenMixin implements EmiSearchInput {
    @Unique private boolean emiSearchInput = false;
    @Unique private boolean emiMouseInput = false;

    @Inject(method = "handleMouseInput", at = @At("HEAD"))
    public void handleMouseInputEMI(CallbackInfo ci) {
        this.emiMouseInput = RetroEMI.handleMouseInput();
    }

    @Inject(method = "handleInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"))
    public void handleKeyboardInputEMI(CallbackInfo ci) {
        this.emiSearchInput = RetroEMI.handleKeyboardInput();
    }

    @Override
    public boolean getEMISearchInput() {
        return this.emiSearchInput;
    }

    @Override
    public boolean getEMIMouseInput() {
        return this.emiMouseInput;
    }
}
