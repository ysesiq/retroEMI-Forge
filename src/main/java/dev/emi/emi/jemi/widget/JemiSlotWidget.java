package dev.emi.emi.jemi.widget;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiIngredientAcceptor;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.text.Text;

public class JemiSlotWidget extends SlotWidget {
	public final JemiRecipeSlot slot;

	public JemiSlotWidget(JemiRecipeSlot slot, EmiRecipe recipe) {
		super(slot.stack, slot.x, slot.y);
		this.slot = slot;
		slot.widget = this;
		if (slot.getRole() == RecipeIngredientRole.OUTPUT) {
			this.recipeContext(recipe);
		}
		this.drawBack(slot.defaultBackground);
		this.customBackground(null, 0, 0, slot.width, slot.height);
	}

	private ITypedIngredient<?> getIngredient() {
		if (slot.renderers != null) {
			Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(getStack().getEmiStacks().get(0));
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
		if (slot.background != null) {
			slot.background.drawable().draw(Minecraft.getMinecraft(), x + slot.background.xOff(), y + slot.background.yOff());
		}
		super.render(raw, mouseX, mouseY, delta);
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void drawStack(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		IIngredientRenderer renderer = getRenderer();
		if (renderer != null) {
			ITypedIngredient<?> typed = getIngredient();
			Bounds bounds = getBounds();
			int xOff = bounds.x() + (bounds.width() - 16) / 2 ;
			int yOff = bounds.y() + (bounds.height() - 16) / 2 ;
			context.enableBlend();
			context.push();
//  		context.matrices().translate(xOff, yOff, 0);
			renderer.render(Minecraft.getMinecraft(), xOff, yOff, typed.ingredient());
			context.pop();
			return;
		}
		super.drawStack(context.raw(), mouseX, mouseY, delta);
	}

	@Override
	public void drawOverlay(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if (slot.overlay != null) {
			context.enableBlend();
			context.push();
			context.matrices().translate(0, 0, 200);
			slot.overlay.drawable().draw(Minecraft.getMinecraft(), slot.x + slot.overlay.xOff(), slot.y + slot.overlay.yOff());
			context.pop();
		}
		super.drawOverlay(context.raw(), mouseX, mouseY, delta);
	}

	@Override
	public void drawSlotHighlight(DrawContext draw, Bounds bounds) {
		EmiRenderHelper.drawSlotHightlight(EmiDrawContext.wrap(draw), bounds.x(), bounds.y(), bounds.width(), bounds.height(), 200);
	}

	@SuppressWarnings("unchecked")
	public static void addTooltip(List<TooltipComponent> list, JemiRecipeSlot slot, EmiIngredient stack, IIngredientRenderer<?> renderer) {
		if (renderer != null) {
			if (stack.getEmiStacks().size() == 1 && stack.getEmiStacks().get(0) instanceof JemiStack js) {
				js = js.copy();
				js.renderer = renderer;
				stack = js;
			}
		}
		list.addAll(stack.getTooltip());
		if (slot.tooltipCallback != null) {
			try {
				List<String> event = Lists.newArrayList();
				List<String> original = stack.getEmiStacks().get(0).getTooltipText().stream().map(Text::getString).collect(Collectors.toList());
				Set<String> toRemove = original.stream().collect(Collectors.toSet());
				event.addAll(original);
				ITypedIngredient<?> typed = JemiUtil.getTyped(stack.getEmiStacks().get(0)).orElse(null);
				if (typed != null) {
					((ITooltipCallback) slot.tooltipCallback).onTooltip(0, slot.isInput(), typed.ingredient(), event);
				}
				int index = Math.min(list.size(), 1);
				if (!event.isEmpty()) {
					List<TooltipComponent> additions = Lists.newArrayList();
					for (String s : event) {
						if (!toRemove.contains(s) && !JemiIngredientAcceptor.FLUID_END.matcher(s).find()) {
							additions.add(TooltipComponent.of(EmiPort.literal(s)));
						}
					}
					list.addAll(index, additions);
				}
			} catch (Exception e) {
				EmiLog.error("Error initializing JEI tooltip", e);
			}
		}
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<TooltipComponent> list = Lists.newArrayList();
		if (getStack().isEmpty()) {
			return shim.java.List.of();
		}
		addTooltip(list, slot, getStack(), getRenderer());
		addSlotTooltip(list);
		return list;
	}
}
