package dev.emi.emi.jemi;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.jemi.impl.JemiIngredients;
import dev.emi.emi.mixin.jei.accessor.BookmarkOverlayAccessor;
import dev.emi.emi.mixin.jei.accessor.GuiIconToggleButtonAccessor;
import dev.emi.emi.screen.RecipeScreen;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.gui.elements.GuiIconButton;
import net.minecraft.client.Minecraft;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import dev.emi.emi.platform.EmiAgnos;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

@SuppressWarnings({"rawtypes", "unchecked"})
public class JemiUtil {

	public static EmiIngredient getIngredient(List<ITypedIngredient<?>> ingredients) {
		if (ingredients.isEmpty()) {
			return EmiStack.EMPTY;
		}
		return EmiIngredient.of(ingredients.stream().map(JemiUtil::getStack).filter(i -> !i.isEmpty()).collect(Collectors.toList()));
	}

	public static EmiStack getStack(Object ingredient) {
		if (JemiPlugin.ingredientRegistry != null) {
			try {
				IIngredientType type = JemiPlugin.ingredientRegistry.getIngredientType(ingredient);
				if (type != null) {
					return getStack(type, ingredient);
				}
			} catch (IllegalArgumentException e) {
			}
		}
		return EmiStack.EMPTY;
	}

	public static EmiStack getStack(ITypedIngredient<?> ingredient) {
		return getStack(ingredient.type(), ingredient.ingredient());
	}

	public static EmiStack getStack(IIngredientType<?> type, Object ingredient) {
		if (ingredient == null || (type != null && !type.getIngredientClass().isInstance(ingredient))) {
			return EmiStack.EMPTY;
		}
		if (type == VanillaTypes.ITEM && ingredient instanceof ItemStack) {
			return EmiStack.of((ItemStack) ingredient);
		} else if (type == getFluidType() || ingredient instanceof FluidStack) {
			return EmiAgnos.createFluidStack(ingredient);
		} else {
			IIngredientRegistry im = JemiPlugin.ingredientRegistry;
			IIngredientHelper helper = im.getIngredientHelper(type);
			if (helper.isValidIngredient(ingredient)) {
				return new JemiStack(type, helper, im.getIngredientRenderer(type), ingredient);
			}
		}
		return EmiStack.EMPTY;
	}

	public static Optional<ITypedIngredient<?>> getTyped(EmiStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		} else if (stack.getKey() instanceof Fluid f) {
			return createTypedIngredient(VanillaTypes.FLUID, new FluidStack(f, (int) (stack.getAmount() == 0 ? 1000 : stack.getAmount()), stack.getNbt()));
		} else if (stack instanceof JemiStack js) {
			return createTypedIngredient(js.type, js.ingredient);
		}
		return createTypedIngredient(VanillaTypes.ITEM, stack.getItemStack());
	}

	public static <T> Optional<ITypedIngredient<?>> createTypedIngredient(IIngredientType<T> type, T ingredient) {
		return Optional.of(new ITypedIngredient(type, ingredient));
	}

	public static IIngredientType<?> getFluidType() {
		return VanillaTypes.FLUID;
	}

	public static Set<String> getHandledMods() {
		Set<String> set = Sets.newHashSet();
		for (String mod : EmiAgnos.getModsWithPlugins()) {
			set.add(mod);
		}
		return set;
	}

	public static GuiIconButton getConfigButton(IJeiRuntime runtime) {
		Object configButton = JemiReflection.getConfigButtonRaw(runtime);
		return ((GuiIconToggleButtonAccessor) configButton).getInternalButton();
	}

	public static GuiIconButton getBookmarkButton(IJeiRuntime runtime) {
		return ((GuiIconToggleButtonAccessor) ((BookmarkOverlayAccessor) runtime.getBookmarkOverlay()).getBookmarkButton()).getInternalButton();
	}

	public static int getConfigButtonWidth(IJeiRuntime runtime) {
		try {
			if (Minecraft.getMinecraft().currentScreen instanceof RecipeScreen) return 0;
			int width = getConfigButton(runtime).getButtonWidth();
			return width > 0 ? width : 20;
		} catch (Exception ignored) {
			return 0;
		}
	}

	public static boolean isDeclared(EmiStack stack, JemiIngredients data, boolean input) {
		ITypedIngredient<?> typed = JemiUtil.getTyped(stack).orElse(null);
		return typed != null && isDeclared(typed, input ? data.getInputs(typed.type()) : data.getOutputs(typed.type()));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static boolean isDeclared(ITypedIngredient<?> typed, List<? extends List<?>> data) {
		try {
			IIngredientHelper helper = JemiIngredients.ingredientRegistry.getIngredientHelper(typed.type());
			return data.stream().anyMatch(slot -> helper.getMatch(slot, typed.ingredient()) != null);
		} catch (Throwable ignored) {
			return false;
		}
	}
}
