package shim.mezz.jei.api.ingredients;

import com.github.bsideup.jabel.Desugar;
import mezz.jei.api.recipe.IIngredientType;

@Desugar
public record ITypedIngredient<T>(IIngredientType<T> type, T ingredient) {
}
