package dev.emi.emi.mixin.early.minecraft.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rewindmc.retroemi.REMIMixinHooks;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryEffectRenderer.class, priority = 2000)
public abstract class InventoryEffectRendererMixin extends GuiContainer {
    @Shadow private boolean field_147045_u;


    public InventoryEffectRendererMixin(Container handlerScreen) {
        super(handlerScreen);
    }

    @WrapOperation(method = "initGui",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;guiLeft:I",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void conNotBeyond(InventoryEffectRenderer instance, int value, Operation<Void> original) {
        guiLeft = Math.max(guiLeft, REMIMixinHooks.EFFECT_WIDTH);
    }

    @Inject(method = "initGui", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;initGui()V", shift = At.Shift.AFTER), cancellable = true)
    private void initGui(CallbackInfo ci) {
        if (EmiConfig.effectLocation == EffectLocation.TOP || EmiConfig.effectLocation == EffectLocation.HIDDEN) {
            this.field_147045_u = false;
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawScreen(IIF)V", shift = At.Shift.AFTER), cancellable = true)
    private void drawScreen(int mouseX, int mouseY, float par3, CallbackInfo ci) {
        if (EmiConfig.effectLocation == EffectLocation.TOP) {
            REMIMixinHooks.drawCenteredEffects((InventoryEffectRenderer) (Object) this, mouseX, mouseY);
            ci.cancel();
        }
    }

    @WrapOperation(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;func_147044_g()V"))
    private void drawEffects(InventoryEffectRenderer instance, Operation<Void> original) {
        REMIMixinHooks.drawEffects(instance);
    }
}
