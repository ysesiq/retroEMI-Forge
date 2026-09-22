package dev.emi.emi.mixin.jei;

import dev.emi.emi.config.EmiConfig;
import mezz.jei.config.Config;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Config.class, remap = false)
public class JemiConfigMixin {
	@Inject(method = "isOverlayEnabled", at = @At("RETURN"), cancellable = true)
	private static void autoHideJei(CallbackInfoReturnable<Boolean> cir) {
		if (EmiConfig.enabled && cir.getReturnValue()) {
			cir.setReturnValue(false);
		}
	}
}
