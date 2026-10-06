package dev.emi.emi.mixin;

import java.util.regex.Matcher;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.rewindmc.retroemi.plugin.REMIMixinHooks;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FontRenderer.class, priority = 2000)
public abstract class FontRendererMixin {
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
}
