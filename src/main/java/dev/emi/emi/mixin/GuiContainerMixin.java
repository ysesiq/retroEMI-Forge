package dev.emi.emi.mixin;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class GuiContainerMixin extends GuiScreen {
    @Shadow public Container inventorySlots;

    @Inject(method = "initGui", at = @At("TAIL"))
    private void addEMIWidgets(CallbackInfo ci) {
        EmiScreenManager.addWidgets(this);
    }
}
