package dev.emi.emi.mixin.accessor;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapedRecipes.class)
public interface ShapedRecipesAccessor {
    @Accessor("recipeWidth")
    int getRecipeWidth();

    @Accessor("recipeHeight")
    int getRecipeHeight();

    @Accessor("recipeItems")
    NonNullList<Ingredient> getRecipeItems();
}
