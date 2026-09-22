package dev.emi.emi.mixin.accessor;

import net.minecraftforge.oredict.ShapedOreRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ShapedOreRecipe.class, remap = false)
public interface ShapedOreRecipeAccessor {
	@Accessor("width")
	int getRecipeWidth();

	@Accessor("height")
	int getRecipeHeight();
}
