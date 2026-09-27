package dev.emi.emi.nemi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.text.Text;

public class NemiRecipeCategory extends EmiRecipeCategory {
    private final String recipeName;

    public NemiRecipeCategory(ResourceLocation id, EmiRenderable icon, String recipeName) {
        super(id, icon);
        this.recipeName = recipeName;
    }

    @Override
    public Text getName() {
        return EmiPort.literal(recipeName);
    }
}
