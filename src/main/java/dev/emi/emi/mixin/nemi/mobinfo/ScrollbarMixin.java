package dev.emi.emi.mixin.nemi.mobinfo;

import codechicken.nei.recipe.GuiRecipe;
import com.kuba6000.mobsinfo.nei.scrollable.Scrollbar;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Scrollbar.class, remap = false)
public class ScrollbarMixin {
	@Inject(method = "beginScissor", at = @At("HEAD"), cancellable = true)
	private void skipScissorOutsideGuiRecipe(int recipe, CallbackInfo ci) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiRecipe) {
			return;
		}
		GL11.glPushMatrix();
		ci.cancel();
	}

	@Inject(method = "endScissor", at = @At("HEAD"), cancellable = true)
	private void skipEndScissorOutsideGuiRecipe(CallbackInfo ci) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiRecipe) {
			return;
		}
		GL11.glPopMatrix();
		ci.cancel();
	}

	@Inject(method = "endBackground", at = @At("HEAD"), cancellable = true)
	private void skipThumbOutsideGuiRecipe(int recipe, CallbackInfo ci) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiRecipe) {
			return;
		}
		GL11.glPopMatrix();
		ci.cancel();
	}

	@Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
	private void skipForegroundOutsideGuiRecipe(int recipe, CallbackInfo ci) {
		if (Minecraft.getMinecraft().currentScreen instanceof GuiRecipe) {
			return;
		}
		ci.cancel();
	}
}
