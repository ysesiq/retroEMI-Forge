package dev.emi.emi.jemi.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IIngredientType;

@SuppressWarnings({"unchecked", "rawtypes"})
public class JemiIngredients implements IIngredients {
	public static IIngredientRegistry ingredientRegistry;
	private final Map<IIngredientType<?>, List<List<?>>> inputs = new LinkedHashMap<>();
	private final Map<IIngredientType<?>, List<List<?>>> outputs = new LinkedHashMap<>();

	@Override
	public <T> void setInput(IIngredientType<T> ingredientType, T ingredient) {
		setInputs(ingredientType, Collections.singletonList(ingredient));
	}

	@Override
	public <T> void setInputs(IIngredientType<T> ingredientType, List<T> ingredients) {
		List<List<T>> slots = Lists.newArrayList();
		for (T ingredient : ingredients) {
			slots.add(Collections.singletonList(ingredient));
		}
		setInputLists(ingredientType, slots);
	}

	@Override
	public <T> void setInputLists(IIngredientType<T> ingredientType, List<List<T>> ingredients) {
		if (ingredientType == null) {
			throw new NullPointerException("ingredientType");
		}
		if (ingredients == null) {
			throw new NullPointerException("ingredients");
		}
		inputs.put(ingredientType, expandSubtypes((List<List<?>>) (List<?>) ingredients));
	}

	@Override
	public <T> void setOutput(IIngredientType<T> ingredientType, T ingredient) {
		setOutputs(ingredientType, Collections.singletonList(ingredient));
	}

	@Override
	public <T> void setOutputs(IIngredientType<T> ingredientType, List<T> ingredients) {
		List<List<T>> slots = Lists.newArrayList();
		for (T ingredient : ingredients) {
			slots.add(Collections.singletonList(ingredient));
		}
		setOutputLists(ingredientType, slots);
	}

	@Override
	public <T> void setOutputLists(IIngredientType<T> ingredientType, List<List<T>> ingredients) {
		if (ingredientType == null) {
			throw new NullPointerException("ingredientType");
		}
		if (ingredients == null) {
			throw new NullPointerException("ingredients");
		}
		outputs.put(ingredientType, expandSubtypes((List<List<?>>) (List<?>) ingredients));
	}

	@Override
	public <T> List<List<T>> getInputs(IIngredientType<T> ingredientType) {
		return castIngredientList(inputs, ingredientType);
	}

	@Override
	public <T> List<List<T>> getOutputs(IIngredientType<T> ingredientType) {
		return castIngredientList(outputs, ingredientType);
	}

	@Override
	public <T> void setInput(Class<? extends T> ingredientClass, T ingredient) {
		setInput(getIngredientType(ingredientClass), ingredient);
	}

	@Override
	public <T> void setInputs(Class<? extends T> ingredientClass, List<T> ingredients) {
		setInputs(getIngredientType(ingredientClass), ingredients);
	}

	@Override
	public <T> void setInputLists(Class<? extends T> ingredientClass, List<List<T>> ingredients) {
		setInputLists(getIngredientType(ingredientClass), ingredients);
	}

	@Override
	public <T> void setOutput(Class<? extends T> ingredientClass, T ingredient) {
		setOutput(getIngredientType(ingredientClass), ingredient);
	}

	@Override
	public <T> void setOutputs(Class<? extends T> ingredientClass, List<T> ingredients) {
		setOutputs(getIngredientType(ingredientClass), ingredients);
	}

	@Override
	public <T> void setOutputLists(Class<? extends T> ingredientClass, List<List<T>> ingredients) {
		setOutputLists(getIngredientType(ingredientClass), ingredients);
	}

	@Override
	public <T> List<List<T>> getInputs(Class<? extends T> ingredientClass) {
		return getInputs(getIngredientType(ingredientClass));
	}

	@Override
	public <T> List<List<T>> getOutputs(Class<? extends T> ingredientClass) {
		return getOutputs(getIngredientType(ingredientClass));
	}

	private <T> List<List<T>> castIngredientList(Map<IIngredientType<?>, List<List<?>>> map, IIngredientType<T> ingredientType) {
		List<List<?>> list = map.get(ingredientType);
		return list == null ? Collections.emptyList() : (List<List<T>>) (List<?>) list;
	}

	private List<List<?>> expandSubtypes(List<List<?>> ingredients) {
		if (ingredientRegistry != null) {
			for (List<?> list : ingredients) {
				if (list == null) {
					continue;
				}
				List<Object> nonNull = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
				if (nonNull.size() > 1) {
					IIngredientHelper helper = ingredientRegistry.getIngredientHelper(nonNull.get(0));
					List expanded = helper.expandSubtypes(list);
					if (expanded != nonNull) {
						List<List<?>> ret = Lists.newArrayList(ingredients);
						ret.set(ingredients.indexOf(list), expanded);
						return ret;
					}
				}
			}
		}
		return ingredients;
	}

	private static IIngredientType getIngredientType(Class ingredientClass) {
		if (ingredientRegistry != null) {
			return ingredientRegistry.getIngredientType(ingredientClass);
		}
		throw new IllegalArgumentException("Unregistered ingredient class: " + ingredientClass);
	}
}
