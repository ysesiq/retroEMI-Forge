package dev.emi.emi.runtime.dev;

import shim.net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.List;

public record RecipeError(Severity severity, List<TooltipComponent> tooltip) {

	public static enum Severity {
		ERROR,
		WARNING
	}
}
