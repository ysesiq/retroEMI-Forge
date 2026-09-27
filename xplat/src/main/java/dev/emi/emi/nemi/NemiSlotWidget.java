package dev.emi.emi.nemi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.item.ItemStack;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;

public class NemiSlotWidget extends SlotWidget {
	private final Supplier<PositionedStack> stackSupplier;
	private final TemplateRecipeHandler handler;
	private final int recipeIndex;

	public NemiSlotWidget(Supplier<PositionedStack> stackSupplier, TemplateRecipeHandler handler, int recipeIndex, int x, int y) {
		super(EmiStack.EMPTY, x, y);
		this.stackSupplier = stackSupplier;
		this.handler = handler;
		this.recipeIndex = recipeIndex;
	}

	@Override
	public EmiIngredient getStack() {
		return NemiRecipe.parseIngredient(stackSupplier.get());
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		List<TooltipComponent> list = super.getTooltip(mouseX, mouseY);
		PositionedStack positionedStack = stackSupplier.get();
		if (positionedStack != null && positionedStack.item != null) {
			ItemStack stack = positionedStack.item;
			List<String> lines = new ArrayList<>();
			try {
				handler.handleItemTooltip(NemiGuiRecipe.instance(), stack, lines, recipeIndex);
			} catch (Exception ignored) {
			}
			for (String line : lines) {
				if (line != null && !line.isEmpty()) {
					list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(line))));
				}
			}
		}
		return list;
	}
}
