package dev.emi.emi.nemi;

import codechicken.nei.recipe.TemplateRecipeHandler;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import shim.net.minecraft.text.Text;

public class NemiCategory extends EmiRecipeCategory {
    public TemplateRecipeHandler handler;
    public String recipeId;

    public NemiCategory(TemplateRecipeHandler handler, String recipeId) {
        super(EmiPort.id(NemiUtil.getModId(handler), recipeId), (raw, x, y, delta) -> {});
        this.handler = handler;
        this.recipeId = recipeId;
        this.icon = NemiUtil.getCategoryIcon(handler);
        this.simplified = this.icon;
    }

    @Override
    public Text getName() {
        return EmiPort.literal(handler.getRecipeName());
    }
}
