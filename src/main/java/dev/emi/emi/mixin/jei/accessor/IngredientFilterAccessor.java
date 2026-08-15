package dev.emi.emi.mixin.jei.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;

@Mixin(value = IngredientFilter.class, remap = false)
public interface IngredientFilterAccessor {
	@Accessor("blacklist") IngredientBlacklistInternal getBlacklist();
}
