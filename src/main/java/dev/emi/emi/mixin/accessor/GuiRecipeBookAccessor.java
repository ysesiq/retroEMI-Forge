package dev.emi.emi.mixin.accessor;

import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.stats.RecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiRecipeBook.class)
public interface GuiRecipeBookAccessor {
    @Accessor("recipeBook")
    RecipeBook getRecipeBook();
}
