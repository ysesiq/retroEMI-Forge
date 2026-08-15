package dev.emi.emi.jemi.runtime;

import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.mixin.jei.accessor.IngredientListOverlayAccessor;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiScreen;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.net.minecraft.client.gui.DrawContext;

public class JemiDragDropHandler implements EmiDragDropHandler<GuiScreen> {

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public boolean dropStack(GuiScreen screen, EmiIngredient stack, int x, int y) {
		try {
			return this.<Object>drop(screen, (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(stack.getEmiStacks().get(0)), x, y);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void render(GuiScreen screen, EmiIngredient dragged, DrawContext raw, int mouseX, int mouseY, float delta) {
		try {
			this.<Object>render(screen, EmiDrawContext.wrap(raw), (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(dragged.getEmiStacks().get(0)));
		} catch (Exception e) {
		}
	}

	private <I> boolean drop(GuiScreen screen, Optional<ITypedIngredient<I>> optional, int x, int y) {
		if (optional.isPresent()) {
			for (IGhostIngredientHandler.Target<I> target : getTargets(screen, optional.get())) {
				if (target.getArea().contains(x, y)) {
					target.accept(optional.get().ingredient());
					return true;
				}
			}
		}
		return false;
	}

	private <I> void render(GuiScreen screen, EmiDrawContext context, Optional<ITypedIngredient<I>> optional) {
		if (optional.isPresent()) {
			for (IGhostIngredientHandler.Target<I> target : getTargets(screen, optional.get())) {
				Rectangle r = target.getArea();
				context.fill(r.x, r.y, r.width, r.height, 0x8822BB33);
			}
		}
	}

	private <I> List<IGhostIngredientHandler.Target<I>> getTargets(GuiScreen screen, ITypedIngredient<I> typed) {
		IGhostIngredientHandler<GuiScreen> ghost = ((IngredientListOverlayAccessor) JemiPlugin.runtime.getIngredientListOverlay()).getGuiScreenHelper().getGhostIngredientHandler(screen);
		if (ghost != null) {
			return ghost.getTargets(screen, typed.ingredient(), false);
		}
		return shim.java.List.of();
	}
}
