package dev.emi.emi.nemi;

import java.lang.reflect.Field;

import codechicken.nei.recipe.TemplateRecipeHandler;

public class NemiReflection {

	public static String getTransferRectOutputId(TemplateRecipeHandler.RecipeTransferRect rect) {
		try {
			Field outputIdField = TemplateRecipeHandler.RecipeTransferRect.class.getDeclaredField("outputId");
			outputIdField.setAccessible(true);
			return (String) outputIdField.get(rect);
		} catch (Exception ignored) {
		}
		return null;
	}
}
