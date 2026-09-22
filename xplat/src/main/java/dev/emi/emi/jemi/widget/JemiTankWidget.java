package dev.emi.emi.jemi.widget;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;

public class JemiTankWidget extends TankWidget {
	private final JemiRecipeSlot slot;
	private final JemiSlotWidget jsw;

	public JemiTankWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
		super(slot.getStack(), slot.x - 1, slot.y - 1,
				(slot.tankInfo != null ? slot.tankInfo.width() : slot.width) + 2,
				(slot.tankInfo != null ? slot.tankInfo.height() : slot.height) + 2,
				slot.tankInfo != null ? slot.tankInfo.capacity() : slot.stack.getAmount());
		this.slot = slot;
		slot.widget = this;
		if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
			this.recipeContext(recipe);
		}
		this.drawBack(false);
		this.jsw = new JemiSlotWidget(slot, recipe);
	}

	private ITypedIngredient<?> getIngredient() {
		if (slot.renderers != null && !slot.stack.isEmpty()) {
			Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(slot.stack.getEmiStacks().get(0));
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private IIngredientRenderer<?> getRenderer() {
		ITypedIngredient<?> typed = getIngredient();
		if (typed != null) {
			if (slot.renderers.containsKey(typed.type())) {
				return slot.renderers.get(typed.type()).renderer();
			}
		}
		return null;
	}

	@Override
	public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (slot.background != null) {
			slot.background.drawable().draw(Minecraft.getMinecraft(), x + 1 + slot.background.xOff(), y + 1 + slot.background.yOff());
		}
		super.render(context.raw(), mouseX, mouseY, delta);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void drawStack(DrawContext raw, int mouseX, int mouseY, float delta) {
		IIngredientRenderer renderer = getRenderer();
		if (renderer != null) {
			ITypedIngredient<?> typed = getIngredient();
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			context.enableBlend();
			renderer.render(Minecraft.getMinecraft(), slot.x, slot.y, typed.ingredient());
			return;
		}
		super.drawStack(raw, mouseX, mouseY, delta);
	}

	@Override
	public void drawOverlay(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (slot.overlay != null) {
			context.enableBlend();
			context.push();
			context.matrices().translate(0, 0, 200);
			slot.overlay.drawable().draw(Minecraft.getMinecraft(), x + 1 + slot.overlay.xOff(), y + 1 + slot.overlay.yOff());
			context.pop();
		}
		super.drawOverlay(context.raw(), mouseX, mouseY, delta);
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return jsw.getTooltip(mouseX, mouseY);
	}
}
