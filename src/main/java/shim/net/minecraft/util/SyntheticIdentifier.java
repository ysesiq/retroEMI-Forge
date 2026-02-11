package shim.net.minecraft.util;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.mixin.accessor.ShapedOreRecipeAccessor;
import dev.emi.emi.mixin.accessor.ShapedRecipesAccessor;
import dev.emi.emi.mixin.accessor.ShapelessRecipesAccessor;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyntheticIdentifier {

    public static ResourceLocation generateId(Object o) {
        if (o == null) {
            return EmiPort.id( "null:null");
        } else if (o instanceof ShapedRecipes sr) {
            return  EmiPort.id("shaped:" + ((ShapedRecipesAccessor) sr).getRecipeWidth() + "x" + ((ShapedRecipesAccessor) sr).getRecipeHeight() + "/" + describeFlat(((ShapedRecipesAccessor) sr).getRecipeItems()) + "/" +
                describe(sr.getRecipeOutput()));
        } else if (o instanceof ShapelessRecipes sr) {
            return  EmiPort.id("shapeless:" + describeFlat(((ShapelessRecipesAccessor)sr).getRecipeItems()) + "/" + describe(sr.getRecipeOutput()));
        } else if (o instanceof ShapedOreRecipe sr) {
            return  EmiPort.id("shaped_ore:" + ((ShapedOreRecipeAccessor) sr).getRecipeWidth() + "x" + ((ShapedOreRecipeAccessor) sr).getRecipeHeight() + "/" +
                describe(sr.getRecipeOutput().getUnlocalizedName() + "." + sr.getRecipeOutput().getItemDamage()));
        } else if (o instanceof ShapelessOreRecipe sr) {
            return  EmiPort.id("shapeless_ore:" + describe(sr.getRecipeOutput().getUnlocalizedName() + "." + sr.getRecipeOutput().getItemDamage()));
        } else if (o instanceof EmiCraftingRecipe cr) {
            return  EmiPort.id("crafting:" + describeFlat(cr.getInputs()) + "/" + describe(cr.getOutputs()));
        }
        return  EmiPort.id("unknown:/" + describe(o));
    }

    public static String describeFlat(List<?> li) {
        return describeFlat(li.stream());
    }

    public static String describeFlat(Object[] items) {
        return describeFlat(Arrays.stream(items));
    }

    public static String describeFlat(Stream<?> stream) {
        return stream.map(SyntheticIdentifier::describe).collect(Collectors.joining("/"));
    }

    public static String describe(Object o) {
        if (o == null) {
            return "null";
        } else if (o instanceof EmiStack es) {
            return describe(es.getItemStack());
        } else if (o instanceof EmiIngredient ei) {
            return ei.getEmiStacks().stream().map(SyntheticIdentifier::describe).collect(Collectors.joining("/", "[", "]"));
        } else if (o instanceof ItemStack is) {
            if (is.getItem() == null) {
                return "null";
            } else {
                return is.getUnlocalizedName() + "." + is.getItemDamage() + (is.hasTagCompound() ? is.getTagCompound().toString() : "");
            }
        } else if (o instanceof Block b) {
            return describe(new ItemStack(b));
        } else if (o instanceof String s) {
            return s;
        } else if (o instanceof List<?> l) {
            return l.stream().map(SyntheticIdentifier::describe).collect(Collectors.joining("/", "[", "]"));
        } else if (o instanceof Object[] arr) {
            return Arrays.stream(arr).map(SyntheticIdentifier::describe).collect(Collectors.joining("/", "[", "]"));
//        } else if (o instanceof Prototype p) {
//            return p.item() == null ? "0.0" : Item.getIdFromItem(p.item()) + "." + p.meta();
        } else {
            return o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o));
        }
    }
}
