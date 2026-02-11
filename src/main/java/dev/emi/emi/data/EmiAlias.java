package dev.emi.emi.data;

import com.github.bsideup.jabel.Desugar;
import dev.emi.emi.api.stack.EmiIngredient;
import shim.net.minecraft.text.Text;

import java.util.List;

@Desugar
public record EmiAlias(List<EmiIngredient> stacks, List<String> keys) {

    @Desugar
    public static record Baked(List<EmiIngredient> stacks, List<Text> text) {
    }
}
