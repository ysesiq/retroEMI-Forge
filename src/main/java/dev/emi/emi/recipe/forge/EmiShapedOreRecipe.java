package dev.emi.emi.recipe.forge;

import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiShapedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class EmiShapedOreRecipe extends EmiCraftingRecipe {

    public EmiShapedOreRecipe(ShapedOreRecipe recipe) {
        super(padIngredients(recipe), EmiStack.of(recipe.getRecipeOutput()), EmiPort.getId(recipe), false);
        EmiShapedRecipe.setRemainders(input, recipe);
    }

    private static List<EmiIngredient> padIngredients(ShapedOreRecipe recipe) {
        List<EmiIngredient> list = Lists.newArrayList();
        int i = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int width = recipe.getRecipeWidth();
                int height = recipe.getRecipeHeight();
                if (x >= width || y >= height || i >= recipe.getIngredients().size()) {
                    list.add(EmiStack.EMPTY);
                } else {
                    list.add(fromOreInput(recipe.getIngredients().get(i++)));
                }
            }
        }
        return list;
    }

    public static EmiIngredient fromOreInput(Object input) {
        if (input instanceof ItemStack stack) {
            return EmiStack.ofPotentialTag(stack);
        } else if (input instanceof List<?> list) {
            if (list.isEmpty()) {
                return EmiStack.EMPTY;
            } else {
                if (list.getFirst() instanceof ItemStack) {
                    return EmiIngredient.of(streamForgeList(list).map(stack -> (ItemStack) stack).map(EmiStack::ofPotentialTag).collect(Collectors.toList()));
                }
            }
        } else if (input == null) {
            return EmiStack.EMPTY;
        }
        throw new IllegalArgumentException("Unknown Oredict input type: " + input);
    }

    //Forge uses a broken custom list for oredict
    private static <T> Stream<T> streamForgeList(List<T> list) {
        return StreamSupport.stream(Spliterators.spliterator(list, 0), false);
    }
}
