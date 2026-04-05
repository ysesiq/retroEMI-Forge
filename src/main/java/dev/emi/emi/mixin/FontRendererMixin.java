package dev.emi.emi.mixin;

import com.rewindmc.retroemi.REMIMixinHooks;
import net.minecraft.client.gui.FontRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FontRenderer.class, priority = 2000)
public abstract class FontRendererMixin {
	@ModifyVariable(
		method = "renderStringAtPos",
			at = @At(
				value = "JUMP",
				opcode = Opcodes.GOTO,
				ordinal = 0
			),
			ordinal = 0 // i
		)
	private int customFontColor(int original, String text, boolean shadow) {
		return REMIMixinHooks.applyCustomFormatCodes((FontRenderer) (Object) this, text, shadow, original);
	}
}
