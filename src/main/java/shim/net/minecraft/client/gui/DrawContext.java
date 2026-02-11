package shim.net.minecraft.client.gui;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import com.rewindmc.retroemi.RetroEMI;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;

public class DrawContext {

	public static final DrawContext INSTANCE = new DrawContext();

	private DrawContext() {}

	public MatrixStack getMatrices() {
		return MatrixStack.INSTANCE;
	}

	public void drawItem(ItemStack stack, int x, int y) {
		if (stack == null) return;
//		glColor4f(1, 1, 1, 1);
		RetroEMI.instance.itemRenderer.zLevel += 100;
		RetroEMI.instance.itemRenderer.renderItemAndEffectIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().renderEngine, stack, x, y);
		RetroEMI.instance.itemRenderer.zLevel -= 100;
//		glColor4f(1, 1, 1, 1);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public void drawItemInSlot(FontRenderer fontRenderer, ItemStack stack, int x, int y) {
		RetroEMI.instance.itemRenderer.zLevel += 200;
		int count = stack.stackSize;
		stack.stackSize = 1;
		RetroEMI.instance.itemRenderer.renderItemOverlayIntoGUI(fontRenderer, Minecraft.getMinecraft().renderEngine, stack, x, y);
		stack.stackSize = count;
		RetroEMI.instance.itemRenderer.zLevel -= 200;
	}

	public void drawTooltip(FontRenderer fontRenderer, List<Text> txt, int mouseX, int mouseY) {
		EmiRenderHelper.drawTooltip(Minecraft.getMinecraft().currentScreen, EmiDrawContext.instance(), txt.stream().map(TooltipComponent::of).collect(Collectors.toList()), mouseX, mouseY);
	}
}
