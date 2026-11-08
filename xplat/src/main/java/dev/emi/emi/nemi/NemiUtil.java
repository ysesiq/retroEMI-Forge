package dev.emi.emi.nemi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.nemi.impl.NemiRenderable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class NemiUtil {
	private static final EmiStack DEFAULT_ICON = EmiStack.of(Blocks.crafting_table);
	private static final Map<Class<?>, List<Field>> FLUID_FIELDS = new HashMap<>();

	public static EmiIngredient parseIngredient(PositionedStack positionedStack) {
		if (positionedStack == null) {
			return EmiStack.EMPTY;
		}

		List<EmiIngredient> ingredients = new ArrayList<>();

		if (positionedStack.items != null && positionedStack.items.length > 0) {
			for (ItemStack stack : positionedStack.items) {
				if (stack != null) {
					// ofPotentialTag expands wildcard metadata used heavily by NEI
					ingredients.add(EmiStack.ofPotentialTag(stack));
				}
			}
		} else if (positionedStack.item != null) {
			ingredients.add(EmiStack.ofPotentialTag(positionedStack.item));
		}

		if (ingredients.isEmpty()) {
			return EmiStack.EMPTY;
		}

		return EmiIngredient.of(ingredients);
	}

	public static List<EmiIngredient> parseFluidIngredients(Object cachedRecipe) {
		List<EmiIngredient> ingredients = new ArrayList<>();
		for (Field field : getFluidFields(cachedRecipe.getClass())) {
			try {
				if (field.get(cachedRecipe) instanceof FluidStack stack && stack.getFluid() != null) {
					ingredients.add(EmiStack.of(stack));
				}
			} catch (ReflectiveOperationException ignored) {
			}
		}

		return ingredients;
	}

	private static List<Field> getFluidFields(Class<?> type) {
		synchronized (FLUID_FIELDS) {
			List<Field> fields = FLUID_FIELDS.get(type);
			if (fields != null) {
				return fields;
			}

			fields = new ArrayList<>();
			for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
				for (Field field : current.getDeclaredFields()) {
					if (Modifier.isStatic(field.getModifiers()) || !FluidStack.class.isAssignableFrom(field.getType())) {
						continue;
					}
					try {
						field.setAccessible(true);
						fields.add(field);
					} catch (RuntimeException ignored) {
					}
				}
			}

			FLUID_FIELDS.put(type, fields);
			return fields;
		}
	}

	public static String getModId(TemplateRecipeHandler handler) {
		HandlerInfo info = GuiRecipeTab.getHandlerInfo(handler);
		if (info == null || info.getModId() == null) return NemiPlugin.DOMAIN;
		return info.getModId();
	}

	public static EmiRenderable getCategoryIcon(TemplateRecipeHandler handler) {
		HandlerInfo info = GuiRecipeTab.getHandlerInfo(handler);
		if (info == null) return DEFAULT_ICON;
		if (info.getItemStack() != null) {
			return EmiStack.of(info.getItemStack());
		} else if (info.getImage() != null) {
			return new NemiRenderable(info.getImage());
		}
		return DEFAULT_ICON;
	}
}
