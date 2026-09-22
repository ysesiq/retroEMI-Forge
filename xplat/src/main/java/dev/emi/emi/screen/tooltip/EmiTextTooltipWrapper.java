package dev.emi.emi.screen.tooltip;

import dev.emi.emi.api.stack.EmiIngredient;
import shim.net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import shim.net.minecraft.text.OrderedText;

public class EmiTextTooltipWrapper extends OrderedTextTooltipComponent {
	public EmiIngredient stack;

	public EmiTextTooltipWrapper(EmiIngredient stack, OrderedText text) {
		super(text);
		this.stack = stack;
	}

	public EmiTextTooltipWrapper(EmiIngredient stack, OrderedTextTooltipComponent original) {
		super(original.getText());
		this.stack = stack;
	}
}
