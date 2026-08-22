package dev.emi.emi.jemi.impl;

import java.awt.Color;
import java.util.List;
import com.github.bsideup.jabel.Desugar;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.jemi.JemiUtil;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.gui.ingredients.CycleTimer;
import net.minecraft.client.Minecraft;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;

public class JemiRecipeSlot implements IGuiIngredient<Object> {
	public final RecipeIngredientRole role;
	public final boolean large, defaultBackground;
	public final int x, y;
	public final int width, height;
	public final Optional<String> name;
	public final ITooltipCallback<?> tooltipCallback;
//	public final IRecipeSlotRichTooltipCallback richTooltipCallback;
	public final OffsetDrawable background, overlay;
	public final Map<IIngredientType<?>, IngredientRenderer<?>> renderers;
	public final TankInfo tankInfo;
	public final EmiIngredient stack;
	public final int slotIndex;
	public final IIngredientType<?> ingredientType;
	public IRecipeLayoutDrawable layout;
	public SlotWidget widget;
	public int highlight = 0;
	private final CycleTimer cycleTimer = new CycleTimer(0);

	public JemiRecipeSlot(JemiRecipeSlotBuilder builder) {
		this.role = builder.acceptor.role;
		this.large = builder.large;
		this.defaultBackground = builder.defaultBackground;
		this.x = builder.x;
		this.y = builder.y;
		this.width = builder.width;
		this.height = builder.height;
		this.name = builder.name;
		this.tooltipCallback = builder.tooltipCallback;
//		this.richTooltipCallback = builder.richTooltipCallback;
		this.background = builder.background;
		this.overlay = builder.overlay;
		this.renderers = builder.renderers;
		this.tankInfo = builder.tankInfo;
		this.stack = builder.acceptor.build();
		this.slotIndex = builder.slotIndex;
		this.ingredientType = builder.ingredientType;
	}

	public JemiRecipeSlot(RecipeIngredientRole role, EmiStack stack) {
		this.role = role;
		this.large = false;
		this.defaultBackground = false;
		this.x = 0;
		this.y = 0;
		this.width = 18;
		this.height = 18;
		this.name = Optional.empty();
		this.tooltipCallback = null;
//		this.richTooltipCallback = null;
		this.background = null;
		this.overlay = null;
		this.renderers = null;
		this.tankInfo = null;
		this.stack = stack;
		this.slotIndex = 0;
		this.ingredientType = null;
	}

	public <T> Stream<T> getIngredients(IIngredientType<T> ingredientType) {
		return (Stream<T>) getAllIngredientsStream().filter(t -> t.type() == ingredientType).map(t -> t.ingredient());
	}

	public Stream<ITypedIngredient<?>> getAllIngredientsStream() {
		return stack.getEmiStacks().stream().map(JemiUtil::getTyped).filter(Optional::isPresent).map(Optional::get);
	}

	@Override
	public List<Object> getAllIngredients() {
		return getAllIngredientsStream().map(t -> t.ingredient()).collect(Collectors.toList());
	}

	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public Object getDisplayedIngredient() {
		if (stack.getEmiStacks().isEmpty()) {
			return null;
		}
		Optional<ITypedIngredient<?>> ing = JemiUtil.getTyped(stack.getEmiStacks().get(0));
		if (ing.isPresent()/* && ing.get().type() == ingredientType*/) {
			return ing.get().ingredient();
		}
		return null;
	}
//
//	@Override
//	public Optional<JemiTypedIngredient<?>> getDisplayedIngredient() {
//		return JemiUtil.getTyped(stack.getEmiStacks().get(0));
//	}

	public Optional<String> getSlotName() {
		return name;
	}

	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(Minecraft mc, Color color, int xOffset, int yOffset) {
		this.highlight = color.getRGB();
	}

	@Override
	public boolean isInput() {
		return role == RecipeIngredientRole.INPUT;
	}

	public EmiIngredient getStack() {
		if (ingredientType != null && layout != null) {
			IGuiIngredientGroup<?> group = layout.getIngredientsGroup(ingredientType);
			Map<Integer, ? extends IGuiIngredient<?>> gui = group.getGuiIngredients();
			IGuiIngredient<?> gi = gui.get(slotIndex);
			if (gi != null) {
				cycleTimer.onDraw();
				Object cycled = cycleTimer.getCycledItem(gi.getAllIngredients());
				if (cycled != null) {
					EmiStack es = JemiUtil.getStack(ingredientType, cycled);
					if (!es.isEmpty()) {
						return es;
					}
				}
				Object displayed = gi.getDisplayedIngredient();
				if (displayed != null) {
					return JemiUtil.getStack(ingredientType, displayed);
				}
			}
		}
		return stack;
	}


	@Desugar
	public static record OffsetDrawable(IDrawable drawable, int xOff, int yOff){
	}

	@Desugar
	public static record IngredientRenderer<T>(IIngredientType<T> type, IIngredientRenderer<T> renderer){
	}

	@Desugar
	public static record TankInfo(int width, int height, long capacity, boolean showCapacity) {
	}
}
