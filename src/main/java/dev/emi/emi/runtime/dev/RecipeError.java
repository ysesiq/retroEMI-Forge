package dev.emi.emi.runtime.dev;

import com.github.bsideup.jabel.Desugar;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.List;

@Desugar
public record RecipeError(Severity severity, List<TooltipComponent> tooltip) {

	public static enum Severity {
		ERROR,
		WARNING
	}
}
