package com.rewindmc.retroemi.integ;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import com.rewindmc.retroemi.NamedEmiPlugin;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.FluidUnit;
import shim.net.minecraft.text.Text;

public class MiscPlugin implements NamedEmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        EmiStack water = EmiStack.of(Blocks.water, FluidUnit.BUCKET);
        EmiStack lava = EmiStack.of(Blocks.lava, FluidUnit.BUCKET);
        EmiStack waterCatalyst = water.copy().setRemainder(water);
        EmiStack lavaCatalyst = lava.copy().setRemainder(lava);

        registry.addRecipe(EmiWorldInteractionRecipe.builder()
                .id(new ResourceLocation("emi", "/world/fluid_interaction/minecraft/obsidian_glitch"))
                .leftInput(waterCatalyst)
                .rightInput(EmiStack.of(Items.redstone), false, (sw) -> {
                    sw.appendTooltip(Text.literal(String.valueOf('\u00a7') + "6Build a cobblestone generator, and put redstone dust where the cobblestone would generate."));
                    return sw;
                })
                .rightInput(lavaCatalyst, false)
                .output(EmiStack.of(Blocks.obsidian))
                .supportsRecipeTree(true)
                .build());
    }

    @Override
    public String getName() {
        return "retro";
    }

}
