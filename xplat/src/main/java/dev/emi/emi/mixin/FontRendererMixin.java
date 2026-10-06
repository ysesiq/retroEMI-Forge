package dev.emi.emi.mixin;

import java.util.regex.Matcher;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.rewindmc.retroemi.plugin.REMIMixinHooks;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FontRenderer.class, priority = 2000)
public abstract class FontRendererMixin {
	@Shadow protected byte[] glyphWidth;

	@WrapMethod(method = "renderStringAtPos")
	private void customFontColor(String text, boolean shadow, Operation<Void> original) {
		Matcher matcher = REMIMixinHooks.CUSTOM_FORMAT_CODE.matcher(text);
		int start = 0;
		while (matcher.find()) {
			original.call(text.substring(start, matcher.start() + 2), shadow);
			REMIMixinHooks.applyCustomFormatCodes((FontRenderer) (Object) this, matcher.group(), shadow);
			start = matcher.end();
		}
		original.call(text.substring(start), shadow);
	}

	// fix vanilla bug
	@Inject(method = "readGlyphSizes", at = @At(value = "INVOKE", target = "Ljava/io/InputStream;read([B)I", shift = At.Shift.AFTER))
	private void fixBrackets(CallbackInfo ci) {
		this.glyphWidth['（'] = 127;
	}
}
