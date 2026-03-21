//package dev.emi.emi.jemi.runtime;
//
//import dev.emi.emi.api.EmiDragDropHandler;
//import dev.emi.emi.api.stack.EmiIngredient;
//import dev.emi.emi.jemi.JemiPlugin;
//import dev.emi.emi.jemi.JemiUtil;
//import dev.emi.emi.runtime.EmiDrawContext;
//import mezz.jei.api.gui.IGhostIngredientHandler;
//import net.minecraft.client.gui.GuiScreen;
//import shim.net.minecraft.client.gui.DrawContext;
//
//
//import java.util.List;
//import java.util.Optional;
//
//public class JemiDragDropHandler implements EmiDragDropHandler<GuiScreen> {
//
//	@Override
//	@SuppressWarnings({"rawtypes", "unchecked"})
//	public boolean dropStack(GuiScreen screen, EmiIngredient stack, int x, int y) {
//		try {
//			return this.<Object>drop(screen, (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(stack.getEmiStacks().get(0)), x, y);
//		} catch (Exception e) {
//			return false;
//		}
//	}
//
//	@Override
//	@SuppressWarnings({"rawtypes", "unchecked"})
//	public void render(GuiScreen screen, EmiIngredient dragged, DrawContext raw, int mouseX, int mouseY, float delta) {
//		try {
//			this.<Object>render(screen, EmiDrawContext.wrap(raw), (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(dragged.getEmiStacks().get(0)));
//		} catch (Exception e) {
//		}
//	}
//
//	private <I> boolean drop(GuiScreen screen, Optional<ITypedIngredient<I>> optional, int x, int y) {
//		if (optional.isPresent()) {
//			for (IGhostIngredientHandler.Target<I> target : getTargets(screen, optional.get())) {
//				if (target.getArea().contains(x, y)) {
//					target.accept(optional.get().getIngredient());
//					return true;
//				}
//			}
//		}
//		return false;
//	}
//
//	private <I> void render(GuiScreen screen, EmiDrawContext context, Optional<ITypedIngredient<I>> optional) {
//		if (optional.isPresent()) {
//			for (IGhostIngredientHandler.Target<I> target : getTargets(screen, optional.get())) {
//				Rect2i r = target.getArea();
//				context.fill(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 0x8822BB33);
//			}
//		}
//	}
//
//	private <I> List<IGhostIngredientHandler.Target<I>> getTargets(GuiScreen screen, ITypedIngredient<I> typed) {
//		Optional<IGhostIngredientHandler<GuiScreen>> optGhost = JemiPlugin.runtime.getScreenHelper().getGhostIngredientHandler(screen);
//		if (optGhost.isPresent()) {
//			IGhostIngredientHandler<GuiScreen> ghost = optGhost.get();
//			return ghost.getTargetsTyped(screen, typed, false);
//		}
//		return List.of();
//	}
//}
