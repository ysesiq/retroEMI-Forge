package dev.emi.emi.mixin.accessor;

import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShapelessRecipes.class)
public interface ShapelessRecipesAccessor {
    @Accessor("recipeItems")
    NonNullList<Ingredient> getRecipeItems();
}
