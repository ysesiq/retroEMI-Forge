package dev.emi.emi.platform.forge;

import com.google.common.collect.Lists;
import com.rewindmc.retroemi.EmiModAnnotationScanner;
import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.mixin.accessor.MinecraftAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.brewing.AbstractBrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.text.WordUtils;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.registry.tag.ItemKey;
import shim.net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EmiAgnosForge extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosForge();
	}

	@Override
	protected boolean isForgeAgnos() {
		return true;
	}

	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<? extends ModContainer> container = Optional.ofNullable(Loader.instance().getIndexedModList().get(namespace));
		if (container.isPresent()) {
			return container.get().getName();
		}
		container = Optional.ofNullable(Loader.instance().getIndexedModList().get(namespace.replace('_', '-')));
		if (container.isPresent()) {
			return container.get().getName();
		}
		return WordUtils.capitalizeFully(namespace.replace('_', ' '));
	}

	@Override
	protected Path getConfigDirectoryAgnos() {
		return Loader.instance().getConfigDir().toPath();
	}

	@Override
	protected boolean isDevelopmentEnvironmentAgnos() {
		return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}

	@Override
	protected boolean isModLoadedAgnos(String id) {
		return Loader.isModLoaded(id);
	}

	@Override
	protected List<String> getAllModNamesAgnos(String id) {
		return Optional.ofNullable(id)
			.filter(s -> !s.isEmpty())
			.map(modId -> Loader.instance().getActiveModList().stream()
				.filter(container -> modId.equals(container.getModId()))
				.map(container -> container.getMetadata().name)
				.filter(Objects::nonNull)
				.collect(Collectors.toList()))
			.orElse(Collections.emptyList());
	}

	@Override
	protected List<String> getModsWithPluginsAgnos() {
		List<String> mods = Lists.newArrayList();

		for (ModContainer modContainer : Loader.instance().getModList()) {
			if (modContainer instanceof DummyModContainer || (modContainer instanceof InjectedModContainer container && container.wrappedContainer instanceof DummyModContainer)) {
				continue;
			}
			try {
				ClassLoader classLoader = modContainer.getClass().getClassLoader();
				List<Class<?>> annotatedClasses = EmiModAnnotationScanner.scanForAnnotatedClasses(modContainer, classLoader, EmiEntrypoint.class);

				if (!annotatedClasses.isEmpty()) {
					mods.add(modContainer.getModId());
				}
			} catch (Throwable t) {
				EmiLog.error("Exception constructing entrypoint:", t);
			}
		}
		return mods;
	}

	protected List<EmiPluginContainer> getPluginsAgnos() {
		List<EmiPluginContainer> containers = Lists.newArrayList();

		for (ModContainer modContainer : Loader.instance().getModList()) {
			if (modContainer instanceof DummyModContainer || (modContainer instanceof InjectedModContainer container && container.wrappedContainer instanceof DummyModContainer)) {
				continue;
			}
			try {
				ClassLoader classLoader = modContainer.getClass().getClassLoader();
				List<Class<?>> annotatedClasses = EmiModAnnotationScanner.scanForAnnotatedClasses(modContainer, classLoader, EmiEntrypoint.class);

				for (Class<?> clazz : annotatedClasses) {
					if (EmiPlugin.class.isAssignableFrom(clazz)) {
						Class<? extends EmiPlugin> pluginClass = clazz.asSubclass(EmiPlugin.class);
						EmiPlugin plugin = pluginClass.getConstructor().newInstance();
						String id = modContainer.getModId();
						containers.add(new EmiPluginContainer(plugin, id));
					} else {
						EmiLog.error("EmiEntrypoint " + clazz.getName() + " does not implement EmiPlugin");
					}
				}
			} catch (Throwable t) {
				EmiLog.error("Exception constructing entrypoint for mod " + modContainer.getModId() + ":", t);
			}
		}
		return containers;
	}

    @Override
    protected void addBrewingRecipesAgnos(EmiRegistry registry) {
        Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();

        final ItemStack POTION = new ItemStack(Items.POTIONITEM);
        final ItemStack WATER_BOTTLE = PotionUtils.addPotionToItemStack(POTION.copy(), PotionTypes.WATER);

        Set<String> recipeKeys = new HashSet<>();
        Set<String> disabledRecipeKeys = new HashSet<>();

        VanillaBrewingRecipe vanillaRecipe = (VanillaBrewingRecipe) brewingRecipes.stream()
            .filter(r -> r instanceof VanillaBrewingRecipe)
            .findFirst()
            .orElse(null);

        if (vanillaRecipe != null) {
            List<ItemStack> potionIngredients = new ArrayList<>();
            for (Item item : EmiPort.getItemRegistry()) {
                if (item instanceof net.minecraft.item.ItemPotion) continue;

                ItemStack stack = new ItemStack(item);

                for (PotionType type : ForgeRegistries.POTION_TYPES.getValuesCollection()) {
                    ItemStack testPotion = PotionUtils.addPotionToItemStack(POTION.copy(), type);
                    ItemStack out = vanillaRecipe.getOutput(testPotion.copy(), stack);
                    if (!out.isEmpty()) {
                        potionIngredients.add(stack);
                        break;
                    }
                }
            }

            List<ItemStack> knownPotions = new ArrayList<>();
            Set<String> knownPotionKeys = new HashSet<>();

            knownPotions.add(WATER_BOTTLE);
            knownPotionKeys.add(WATER_BOTTLE.getItem().getRegistryName() + "|" +
                ForgeRegistries.POTION_TYPES.getKey(PotionUtils.getPotionFromItem(WATER_BOTTLE)));

            boolean foundNewPotions;
            do {
                List<ItemStack> newPotions = new ArrayList<>();

                for (ItemStack potionInput : knownPotions) {
                    for (ItemStack potionIngredient : potionIngredients) {
                        ItemStack potionOutput = vanillaRecipe.getOutput(potionInput.copy(), potionIngredient);
                        if (potionOutput.isEmpty()) continue;

                        if (potionInput.getItem() == potionOutput.getItem()) {
                            PotionType outType = PotionUtils.getPotionFromItem(potionOutput);
                            if (outType == PotionTypes.WATER) continue;

                            PotionType inType = PotionUtils.getPotionFromItem(potionInput);
                            ResourceLocation inId = ForgeRegistries.POTION_TYPES.getKey(inType);
                            ResourceLocation outId = ForgeRegistries.POTION_TYPES.getKey(outType);
                            if (Objects.equals(inId, outId)) continue;
                        }

                        String key = potionInput.getItem().getRegistryName() + "|" +
                            ForgeRegistries.POTION_TYPES.getKey(PotionUtils.getPotionFromItem(potionInput)) + "->" +
                            potionIngredient.getItem().getRegistryName() + "->" +
                            potionOutput.getItem().getRegistryName() + "|" +
                            ForgeRegistries.POTION_TYPES.getKey(PotionUtils.getPotionFromItem(potionOutput));

                        if (recipeKeys.contains(key) || disabledRecipeKeys.contains(key)) {
                            continue;
                        }

                        if (BrewingRecipeRegistry.hasOutput(potionInput, potionIngredient)) {
                            EmiStack input = EmiStack.of(potionInput);
                            EmiIngredient ingredient = EmiStack.of(potionIngredient);
                            EmiStack output = EmiStack.of(potionOutput);

                            ResourceLocation id = EmiPort.id("emi", "/brewing/vanilla/"
                                + EmiUtil.subId(input.getId()) + "/"
                                + EmiUtil.subId(ingredient.getEmiStacks().get(0).getId()) + "/"
                                + EmiUtil.subId(output.getId()));                            registry.addRecipe(new EmiBrewingRecipe(input, ingredient, output, id));
                            recipeKeys.add(key);

                            String potionKey = potionOutput.getItem().getRegistryName() + "|" +
                                ForgeRegistries.POTION_TYPES.getKey(PotionUtils.getPotionFromItem(potionOutput));

                            if (!knownPotionKeys.contains(potionKey)) {
                                newPotions.add(potionOutput);
                                knownPotionKeys.add(potionKey);
                            }
                        } else {
                            disabledRecipeKeys.add(key);
                        }
                    }
                }

                foundNewPotions = !newPotions.isEmpty();
                knownPotions.addAll(newPotions);
            } while (foundNewPotions);
        }

        for (IBrewingRecipe ibr : BrewingRecipeRegistry.getRecipes()) {
            try {
                if (ibr instanceof AbstractBrewingRecipe recipe && !(ibr instanceof VanillaBrewingRecipe)) {
                    ItemStack is = recipe.getInput();
                    EmiStack input = EmiStack.of(is);
                    EmiIngredient ingredient = EmiIngredient.of((Ingredient) recipe.getIngredient());
                    EmiStack output = EmiStack.of(recipe.getOutput(is, ((Ingredient) recipe.getIngredient()).getMatchingStacks()[0]));
                    ResourceLocation id = EmiPort.id("emi", "/brewing/forge/"
                        + EmiUtil.subId(input.getId()) + "/"
                        + EmiUtil.subId(ingredient.getEmiStacks().get(0).getId()) + "/"
                        + EmiUtil.subId(output.getId()));
                    registry.addRecipe(new EmiBrewingRecipe(input, ingredient, output, id));
                }
            } catch (Exception e) {
                EmiLog.error("Error registering brewing recipe", e);
            }
        }
    }

	@Override
	protected List<String> getAllModAuthorsAgnos(String id) {
		return Optional.ofNullable(id)
			.filter(s -> !s.isEmpty())
			.map(modId -> Loader.instance().getActiveModList().stream()
				.filter(container -> modId.equals(container.getModId()))
				.flatMap(container -> container.getMetadata().authorList.stream())
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList()))
			.orElse(Collections.emptyList());
	}

	@Override
	protected List<TooltipComponent> getItemTooltipAgnos(ItemStack stack) {
		List<String> tip = stack.getTooltip(Minecraft.getMinecraft().player, ITooltipFlag.TooltipFlags.ADVANCED);
		return tip.stream()
				.map(Text::literal).map(TooltipComponent::of)
				.collect(java.util.stream.Collectors.toList());
	}

	@Override
	protected Text getFluidNameAgnos(Fluid fluid, NBTTagCompound nbt) {
		return Text.literal(new FluidStack(fluid, 1000, nbt).getLocalizedName());
	}

	@Override
	protected List<Text> getFluidTooltipAgnos(Fluid fluid, NBTTagCompound componentChanges) {
		List<Text> tooltip = Lists.newArrayList();
		tooltip.add(getFluidName(fluid, componentChanges));
		Minecraft client = Minecraft.getMinecraft();
		if (client.gameSettings.advancedItemTooltips) {
			tooltip.add(EmiPort.literal(fluid.getName()).formatted(TextFormatting.DARK_GRAY));
		}
		return tooltip;
	}

	@Override
	protected boolean isFloatyFluidAgnos(FluidEmiStack stack) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class), 1000, stack.getNbt());
		return fs.getFluid().getDensity() <= 0;
	}

	@Override
	protected void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class), 1000, stack.getNbt());
		Fluid ext = fs.getFluid();
		ResourceLocation texture = ext.getStill();
		if (texture == null) {
			return;
		}
		int color = ext.getColor(fs);
		RenderSystem.setShaderTexture(0, TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(texture.toString());
		EmiRenderHelper.drawTintedSprite(matrices, sprite, color, x, y, xOff, yOff, width, height);
	}

	@Override
	protected EmiStack createFluidStackAgnos(Object object) {
		if (object instanceof FluidStack f) {
			return EmiStack.of(f.getFluid(), f.tag, f.amount);
		}
		return EmiStack.EMPTY;
	}

	@Override
	protected boolean canBatchAgnos(ItemStack stack) {
		return false;
	}

	@Override
	protected Map<ItemKey, Integer> getFuelMapAgnos() {
		Map<ItemKey, Integer> fuelMap = new HashMap<>();
		for (Item item : RetroEMI.getAllItems()) {
			NonNullList<ItemStack> stacks = NonNullList.create();
			item.getSubItems(CreativeTabs.MISC, stacks);
			for (ItemStack stack : stacks) {
				int time = TileEntityFurnace.getItemBurnTime(stack);
				if (time > 0) {
					fuelMap.put(ItemKey.of(stack), time);
				}
			}
		}
		return fuelMap;
	}

	@Override
	protected IBakedModel getBakedTagModelAgnos(ResourceLocation id) {
		return ((MinecraftAccessor) Minecraft.getMinecraft()).getModelManager().getModel(new ModelResourceLocation(id, "inventory"));
	}

	@Override
	protected boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment) {
		ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
		EnchantmentHelper.setEnchantments(Collections.singletonMap(enchantment, enchantment.getMaxLevel()), enchantedBook);
		return stack.getItem().isBookEnchantable(stack, enchantedBook);
	}
}
