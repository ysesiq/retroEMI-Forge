package dev.emi.emi.mixin.early.minecraft.client;

import dev.emi.emi.screen.EmiScreenManager;
import net.xylose.emi.inject_interface.EmiSearchInput;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.rewindmc.retroemi.REMIMixinHooks;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen {
    @Shadow public Container inventorySlots;

    @Inject(method = "initGui", at = @At("TAIL"))
    private void addEMIWidgets(CallbackInfo ci) {
        EmiScreenManager.addWidgets(this);
    }

    @Inject(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawDefaultBackground()V",
                    shift = At.Shift.AFTER
            ))
    private void renderEMIBackground(int par1, int par2, float par3, CallbackInfo ci) {
        REMIMixinHooks.renderBackground(par1, par2); //render EMI background
    }

    @Inject(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerForegroundLayer(II)V",
                    shift = At.Shift.AFTER
            ))
    private void renderForegroundPost(int par1, int par2, float par3, CallbackInfo ci) {
        REMIMixinHooks.renderForegroundPre(par1, par2, this.mc);
        REMIMixinHooks.renderForegroundPost(par1, par2, this.mc);
    }

    @Inject(method = "func_146977_a", at = @At(value = "RETURN"))
    private void drawSlot(Slot par1Slot, CallbackInfo ci) {
        REMIMixinHooks.drawSlot(par1Slot);
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    public void disableHotkeyInEMISearchInput(char par1, int par2, CallbackInfo ci) {
        if (((EmiSearchInput) this).getEMISearchInput()) {
            ci.cancel();
        }
    }
}
