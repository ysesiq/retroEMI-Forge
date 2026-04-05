package dev.emi.emi;

import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.ANVIL_REPAIRING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.BREWING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.CRAFTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.FUEL;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.INFO;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.SMELTING;
import static dev.emi.emi.api.recipe.VanillaEmiRecipeCategories.WORLD_INTERACTION;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiRecipeSorting;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.config.FluidUnit;
import dev.emi.emi.handler.CookingRecipeHandler;
import dev.emi.emi.handler.CraftingRecipeHandler;
import dev.emi.emi.handler.InventoryRecipeHandler;
import dev.emi.emi.mixin.accessor.ItemToolAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiAnvilRecipe;
import dev.emi.emi.recipe.EmiCookingRecipe;
import dev.emi.emi.recipe.EmiFuelRecipe;
import dev.emi.emi.recipe.EmiShapedRecipe;
import dev.emi.emi.recipe.EmiShapelessRecipe;
import dev.emi.emi.recipe.EmiTagRecipe;
import dev.emi.emi.recipe.forge.EmiShapelessOreRecipe;
import dev.emi.emi.recipe.special.EmiAnvilEnchantRecipe;
import dev.emi.emi.recipe.special.EmiAnvilRepairItemRecipe;
import dev.emi.emi.recipe.special.EmiArmorDyeRecipe;
import dev.emi.emi.recipe.special.EmiBannerDuplicateRecipe;
import dev.emi.emi.recipe.special.EmiBannerShieldRecipe;
import dev.emi.emi.recipe.special.EmiBookCloningRecipe;
import dev.emi.emi.recipe.special.EmiFireworkRocketRecipe;
import dev.emi.emi.recipe.special.EmiFireworkStarFadeRecipe;
import dev.emi.emi.recipe.special.EmiFireworkStarRecipe;
import dev.emi.emi.recipe.special.EmiMapCloningRecipe;
import dev.emi.emi.recipe.special.EmiRepairItemRecipe;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.runtime.EmiTagKey;
import dev.emi.emi.stack.serializer.FluidEmiStackSerializer;
import dev.emi.emi.stack.serializer.ItemEmiStackSerializer;
import dev.emi.emi.stack.serializer.ListEmiIngredientSerializer;
import dev.emi.emi.stack.serializer.TagEmiIngredientSerializer;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeBookCloning;
import net.minecraft.item.crafting.RecipeFireworks;
import net.minecraft.item.crafting.RecipeRepairItem;
import net.minecraft.item.crafting.RecipeTippedArrow;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.item.crafting.RecipesBanners;
import net.minecraft.item.crafting.RecipesMapCloning;
import net.minecraft.item.crafting.RecipesMapExtending;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.item.crafting.ShieldRecipes;
import net.minecraft.item.crafting.ShulkerBoxRecipes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import shim.net.minecraft.item.DyeItem;
import shim.net.minecraft.recipe.CookingRecipe;
import shim.net.minecraft.registry.tag.ItemKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import shim.net.minecraft.registry.tag.TagKey;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@EmiEntrypoint
public class VanillaPlugin implements EmiPlugin {
	public static EmiRecipeCategory TAG = new EmiRecipeCategory(EmiPort.id("emi:tag"),
		EmiStack.of(Items.NAME_TAG), simplifiedRenderer(240, 208), EmiRecipeSorting.none());

	public static EmiRecipeCategory INGREDIENT = new EmiRecipeCategory(EmiPort.id("emi:ingredient"),
		EmiStack.of(Items.COMPASS), simplifiedRenderer(240, 208));
	public static EmiRecipeCategory RESOLUTION = new EmiRecipeCategory(EmiPort.id("emi:resolution"),
		EmiStack.of(Items.COMPASS), simplifiedRenderer(240, 208));

	static {
		CRAFTING = new EmiRecipeCategory(EmiPort.id("minecraft:crafting"),
			EmiStack.of(Blocks.CRAFTING_TABLE), simplifiedRenderer(240, 240), EmiRecipeSorting.compareOutputThenInput());
		SMELTING = new EmiRecipeCategory(EmiPort.id("minecraft:smelting"),
			EmiStack.of(Blocks.FURNACE), simplifiedRenderer(224, 240), EmiRecipeSorting.compareOutputThenInput());
		ANVIL_REPAIRING = new EmiRecipeCategory(EmiPort.id("emi:anvil_repairing"),
			EmiStack.of(Blocks.ANVIL), simplifiedRenderer(240, 224), EmiRecipeSorting.none());
		BREWING = new EmiRecipeCategory(EmiPort.id("minecraft:brewing"),
			EmiStack.of(Items.BREWING_STAND), simplifiedRenderer(224, 224), EmiRecipeSorting.none());
		WORLD_INTERACTION = new EmiRecipeCategory(EmiPort.id("emi:world_interaction"),
			EmiStack.of(Blocks.GRASS), simplifiedRenderer(208, 224), EmiRecipeSorting.none());
		EmiRenderable flame = (matrices, x, y, delta) -> {
			EmiTexture.FULL_FLAME.render(matrices, x + 1, y + 1, delta);
		};
		FUEL = new EmiRecipeCategory(EmiPort.id("emi:fuel"), flame, flame, EmiRecipeSorting.compareInputThenOutput());
		INFO = new EmiRecipeCategory(EmiPort.id("emi:info"),
			EmiStack.of(Items.WRITABLE_BOOK), simplifiedRenderer(208, 224), EmiRecipeSorting.none());
	}


	@Override
	public void initialize(EmiInitRegistry registry) {
		registry.addIngredientSerializer(ItemEmiStack.class, new ItemEmiStackSerializer());
		registry.addIngredientSerializer(FluidEmiStack.class, new FluidEmiStackSerializer());
		registry.addIngredientSerializer(TagEmiIngredient.class, new TagEmiIngredientSerializer());
		registry.addIngredientSerializer(ListEmiIngredient.class, new ListEmiIngredientSerializer());

		registry.addRegistryAdapter(EmiRegistryAdapter.simple(ItemKey.class, TagKey.Type.ITEM, (key, nbt, amount) -> EmiStack.of(key.item(), nbt, amount, key.meta())));
		registry.addRegistryAdapter(EmiRegistryAdapter.simple(Fluid.class, TagKey.Type.FLUID, EmiStack::of));
	}

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(CRAFTING);
		registry.addCategory(SMELTING);
		registry.addCategory(ANVIL_REPAIRING);
		registry.addCategory(BREWING);
		registry.addCategory(WORLD_INTERACTION);
		registry.addCategory(FUEL);
		registry.addCategory(INFO);
		registry.addCategory(TAG);
		registry.addCategory(INGREDIENT);
		registry.addCategory(RESOLUTION);

		registry.addWorkstation(CRAFTING, EmiStack.of(Blocks.CRAFTING_TABLE));
		registry.addWorkstation(SMELTING, EmiStack.of(Blocks.FURNACE));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(Blocks.ANVIL));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(new ItemStack(Blocks.ANVIL, 1, 1)));
		registry.addWorkstation(ANVIL_REPAIRING, EmiStack.of(new ItemStack(Blocks.ANVIL, 1, 2)));
		registry.addWorkstation(BREWING, EmiStack.of(Items.BREWING_STAND));

		registry.addRecipeHandler(ContainerPlayer.class, new InventoryRecipeHandler());
		registry.addRecipeHandler(ContainerWorkbench.class, new CraftingRecipeHandler());
		registry.addRecipeHandler(ContainerFurnace.class, new CookingRecipeHandler<>(SMELTING));

		registry.addExclusionArea(GuiContainerCreative.class, (screen, consumer) -> {
			int left = screen.getGuiLeft();
			int top = screen.getGuiTop();
			int width = screen.getXSize();
			int bottom = top + screen.getYSize();
			consumer.accept(new Bounds(left, top - 28, width, 28));
			consumer.accept(new Bounds(left, bottom, width, 28));
		});

		registry.addGenericExclusionArea((screen, consumer) -> {
			if (EmiConfig.effectLocation != EffectLocation.HIDDEN && screen instanceof InventoryEffectRenderer inv) {
				Minecraft client = Minecraft.getMinecraft();
				Collection<PotionEffect> collection = client.player.getActivePotionEffects();
				if (!collection.isEmpty()) {
					int k = 33;
					if (collection.size() > 5) {
						k = 132 / (collection.size() - 1);
					}
					int right = inv.getGuiLeft() + inv.getXSize() + 2;
					int rightWidth = inv.width - right;
					if (rightWidth >= 32) {
						int top = inv.getGuiTop();
						int height = (collection.size() - 1) * k + 32;
						int left, width;
						if (EmiConfig.effectLocation == EffectLocation.TOP) {
							int size = collection.size();
							top = inv.getGuiTop() - 34;
							if (screen instanceof GuiContainerCreative) {
								top -= 28;
								if (EmiAgnos.isForge()) {
									top -= 22;
								}
							}
							int xOff = 34;
							if (size == 1) {
								xOff = 122;
							} else if (size > 5) {
								xOff = (inv.getXSize() - 32) / (size - 1);
							}
							width = Math.max(122, (size - 1) * xOff + 32);
							left = inv.getGuiLeft() + (inv.getXSize() - width) / 2;
							height = 32;
						} else {
							left = switch (EmiConfig.effectLocation) {
								case LEFT_COMPRESSED -> inv.getGuiLeft() - 2 - 32;
								case LEFT -> inv.getGuiLeft() - 2 - 120;
								default -> right;
							};
							width = switch (EmiConfig.effectLocation) {
								case LEFT, RIGHT -> 120;
								case LEFT_COMPRESSED, RIGHT_COMPRESSED -> 32;
								default -> 32;
							};
						}
						consumer.accept(new Bounds(left, top, width, height));
					}
				}
			}
		});

		Comparison potionComparison = Comparison.of((a, b) -> RetroEMI.getEffects(a).equals(RetroEMI.getEffects(b)));

		registry.setDefaultComparison(Items.POTIONITEM, potionComparison);
		registry.setDefaultComparison(Items.SPLASH_POTION, potionComparison);
		registry.setDefaultComparison(Items.LINGERING_POTION, potionComparison);
		registry.setDefaultComparison(Items.TIPPED_ARROW, potionComparison);
		registry.setDefaultComparison(Items.ENCHANTED_BOOK, EmiPort.compareStrict());

		Set<Item> hiddenItems = Set.of();
//            Stream.concat(
//			EmiTagKey.of(TagKey.Type.ITEM, EmiTags.HIDDEN_FROM_RECIPE_VIEWERS).getAll().stream().map(itemKey -> ((ItemKey) itemKey).item()),
//			EmiPort.getDisabledItems()
//		).collect(Collectors.toSet());

		List<Item> dyeableItems = RetroEMI.getAllItems().stream().filter(i -> i instanceof ItemArmor armor && armor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER).collect(Collectors.toList());

		for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
			ResourceLocation id = EmiPort.getId(recipe);
			if (recipe instanceof RecipesMapExtending map) {
				EmiStack paper = EmiStack.of(Items.PAPER);
				addRecipeSafe(registry, () -> new EmiCraftingRecipe(shim.java.List.of(
						paper, paper, paper, paper,
						EmiStack.of(Items.FILLED_MAP),
						paper, paper, paper, paper
				),
						EmiStack.of(Items.FILLED_MAP),
						id, false), recipe);
			} else if (recipe instanceof IShapedRecipe shaped && recipe.canFit(3, 3)) {
				addRecipeSafe(registry, () -> new EmiShapedRecipe(shaped), recipe);
			} else if (recipe instanceof ShapelessRecipes shapeless && recipe.canFit(3, 3)) {
				addRecipeSafe(registry, () -> new EmiShapelessRecipe(shapeless), recipe);
			} else if (recipe instanceof ShapelessOreRecipe shapeless && recipe.canFit(3, 3)) {
				addRecipeSafe(registry, () -> new EmiShapelessOreRecipe(shapeless));
			} else if (recipe instanceof RecipesArmorDyes dye) {
				for (Item i : dyeableItems) {
					if (!hiddenItems.contains(i)) {
						addRecipeSafe(registry, () -> new EmiArmorDyeRecipe(i, synthetic("crafting/dying", EmiUtil.subId(i))), recipe);
					}
				}
			} else if (recipe instanceof ShulkerBoxRecipes.ShulkerBoxColoring shulker) {
				for (EnumDyeColor dye : EnumDyeColor.values()) {
					DyeItem dyeItem = DyeItem.byColor(dye);
					ResourceLocation sid = synthetic("crafting/shulker_box_dying", EmiUtil.subId(dyeItem));
					addRecipeSafe(registry, () -> new EmiCraftingRecipe(
						List.of(EmiStack.of(Blocks.PURPLE_SHULKER_BOX), EmiStack.of(dyeItem)),
						EmiStack.of(BlockShulkerBox.getColoredItemStack(dye)), sid), recipe);
				}
			} else if (recipe instanceof ShieldRecipes.Decoration shield) {
				addRecipeSafe(registry, () -> new EmiBannerShieldRecipe(id), recipe);
			} else if (recipe instanceof RecipeBookCloning book) {
				addRecipeSafe(registry, () -> new EmiBookCloningRecipe(id), recipe);
			} else if (recipe instanceof RecipeTippedArrow tipped) {
				EmiPort.getPotionRegistry().forEach(entry -> {
					if (entry == PotionTypes.WATER || entry == PotionTypes.AWKWARD || entry == PotionTypes.MUNDANE || entry == PotionTypes.THICK || entry == PotionTypes.EMPTY) {
						return;
					}
					EmiStack arrow = EmiStack.of(Items.ARROW);
					addRecipeSafe(registry, () -> new EmiCraftingRecipe(List.of(
							arrow, arrow, arrow, arrow,
							EmiStack.of(EmiPort.setPotion(new ItemStack(Items.LINGERING_POTION), entry)),
							arrow, arrow, arrow, arrow
						),
						EmiStack.of(EmiPort.setPotion(new ItemStack(Items.TIPPED_ARROW, 8), entry)),
						synthetic("crafting/tipped_arrow", EmiUtil.subId(EmiPort.getPotionRegistry().getNameForObject(entry))),
						false), recipe);
				});
			} else if (recipe instanceof RecipeFireworks star) {
				addRecipeSafe(registry, () -> new EmiFireworkStarRecipe(id), recipe);
				addRecipeSafe(registry, () -> new EmiFireworkStarFadeRecipe(synthetic("crafting/firework_star_fade", "firework_star_fade")), recipe);
				addRecipeSafe(registry, () -> new EmiFireworkRocketRecipe(synthetic("crafting/firework_rocket", "firework_rocket")), recipe);
			} else if (recipe instanceof RecipesBanners.RecipeDuplicatePattern banner) {
				for (ItemStack i : EmiBannerDuplicateRecipe.BANNERS) {
					if (!hiddenItems.contains(i.getItem())) {
						addRecipeSafe(registry, () -> new EmiBannerDuplicateRecipe(i, synthetic("crafting/banner_copying", EmiUtil.subId(i))), recipe);
					}
				}
			} else if (recipe instanceof RecipeRepairItem tool) {
				for (Item i : EmiRepairItemRecipe.TOOLS) {
					if (!hiddenItems.contains(i)) {
						addRecipeSafe(registry, () -> new EmiRepairItemRecipe(i, synthetic("crafting/repairing", EmiUtil.subId(i))), recipe);
					}
				}
			} else if (recipe instanceof RecipesMapCloning map) {
				addRecipeSafe(registry, () -> new EmiMapCloningRecipe(id), recipe);
			} else if (!(recipe instanceof IRecipe)) {
				try {
					if (!recipe.getIngredients().isEmpty() && !EmiPort.getOutput(recipe).isEmpty() && recipe.canFit(3, 3)) {
						boolean shapeless = recipe.canFit(1, recipe.getIngredients().size()) && recipe.canFit(recipe.getIngredients().size(), 1);
						List<EmiIngredient> input;
						if (shapeless) {
							input = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
						} else {
							int width = recipe.canFit(2, 3) ? recipe.canFit(1, 3) ? 1 : 2 : 3;
							input = Lists.newArrayList();
							for (int i = 0; i < recipe.getIngredients().size(); i++) {
								input.add(EmiIngredient.of(recipe.getIngredients().get(i)));
								if ((i + 1) % width == 0) {
									for (int j = width; j < 3; j++) {
										input.add(EmiStack.EMPTY);
									}
								}
							}
						}
						EmiShapedRecipe.setRemainders(input, recipe);
						addRecipeSafe(registry, () -> new EmiCraftingRecipe(input, EmiStack.of(EmiPort.getOutput(recipe)), id, shapeless));
					}
				} catch (Exception e) {
					EmiReloadLog.warn("Exception when parsing vanilla crafting recipe " + id, e);
				}
			}
		}

		//Smelting recipes are compressed so things like charcoal don't get split, and they are missing tag support like fuel recipes
		Map<ItemKey, ItemKey> smeltingRecipes = new HashMap<>();
		FurnaceRecipes.instance().getSmeltingList().forEach((in, out) -> {
			for (ItemStack stack : EmiStack.ofPotentialTag(in).getEmiStacks().stream().map(EmiStack::getItemStack).toList()) {
				if (smeltingRecipes.put(ItemKey.of(stack), ItemKey.of(out)) != null) {
					throw new IllegalArgumentException("Duplicate smelting recipe: " + in + "=" + out);
				}
			}
		});

		compressRecipesToTags(smeltingRecipes.keySet(), Comparator.comparingInt(stack -> smeltingRecipes.get(stack).hashCode()), tag -> {
			EmiIngredient input = EmiIngredient.of(tag.raw());
			ItemStack output = smeltingRecipes.get(ItemKey.of(input.getEmiStacks().get(0).getItemStack())).toStack();
			addRecipeSafe(registry, () -> new EmiCookingRecipe(new CookingRecipe(input, output,
				FurnaceRecipes.instance().getSmeltingExperience(output)), SMELTING, 1, false));
		}, key -> {
			ItemStack output = smeltingRecipes.get(key).toStack();
			addRecipeSafe(registry, () -> new EmiCookingRecipe(new CookingRecipe(EmiStack.ofPotentialTag(key.toStack()), output,
				FurnaceRecipes.instance().getSmeltingExperience(output)), SMELTING, 1, false));
		});

		safely("repair", () -> addRepair(registry, hiddenItems));
		safely("brewing", () -> EmiAgnos.addBrewingRecipes(registry));
		safely("world interaction", () -> addWorldInteraction(registry, hiddenItems, dyeableItems));
		safely("fuel", () -> addFuel(registry, hiddenItems));

		for (EmiTagKey<?> key : EmiTags.TAGS) {
			if (new TagEmiIngredient(key.raw(), 1).getEmiStacks().size() > 1) {
				addRecipeSafe(registry, () -> new EmiTagRecipe(key.raw()));
			}
		}
	}

	private static void addRepair(EmiRegistry registry, Set<Item> hiddenItems) {
		List<Enchantment> targetedEnchantments = Lists.newArrayList();
		List<Enchantment> universalEnchantments = Lists.newArrayList();
		for (Enchantment enchantment : EmiPort.getEnchantmentRegistry()) {
			try {
				if (enchantment.canApply(ItemStack.EMPTY)) {
					universalEnchantments.add(enchantment);
					continue;
				}
			} catch (Throwable t) {
			}
			targetedEnchantments.add(enchantment);
		}
		for (Item i : EmiPort.getItemRegistry()) {
			if (hiddenItems.contains(i)) {
				continue;
			}
			try {
				if (i.getMaxDamage() > 0) {
					if (i instanceof ItemArmor ai && ai.getArmorMaterial() != null && ai.getArmorMaterial().getRepairItemStack() != null
							&& !ai.getArmorMaterial().getRepairItemStack().isEmpty()) {
                        ResourceLocation id = synthetic("anvil/repairing/material", EmiUtil.subId(i) + "/" + EmiUtil.subId(ai.getArmorMaterial().getRepairItemStack()));
						addRecipeSafe(registry, () -> new EmiAnvilRecipe(EmiStack.of(i), EmiStack.of(ai.getArmorMaterial().getRepairItemStack()), id));
					} else if (i instanceof ItemToolAccessor ti && ti.getToolMaterial().getRepairItemStack() != null
							&& !ti.getToolMaterial().getRepairItemStack().isEmpty()) {
						ResourceLocation id = synthetic("anvil/repairing/material", EmiUtil.subId(i) + "/" + EmiUtil.subId(ti.getToolMaterial().getRepairItemStack().getItem()));
						addRecipeSafe(registry, () -> new EmiAnvilRecipe(EmiStack.of(i), EmiStack.of(ti.getToolMaterial().getRepairItemStack()), id));
					}
				}
				if (i.isDamageable()) {
					addRecipeSafe(registry, () -> new EmiAnvilRepairItemRecipe(i, synthetic("anvil/repairing/tool", EmiUtil.subId(i))));
				}
			} catch (Throwable t) {
				EmiLog.error("Exception thrown registering repair recipes", t);
			}
			try {
				ItemStack defaultStack = i.getDefaultInstance();
				int acceptableEnchantments = 0;
				Consumer<Enchantment> consumer = e -> {
					int max = e.getMaxLevel();
					addRecipeSafe(registry, () -> new EmiAnvilEnchantRecipe(i, e, max,
						synthetic("anvil/enchanting", EmiUtil.subId(i) + "/" + EmiUtil.subId(EmiPort.getEnchantmentRegistry().getNameForObject(e)) + "/" + max)));
				};
				for (Enchantment e : targetedEnchantments) {
					if (e.canApply(defaultStack) && defaultStack.isItemEnchantable()
							&& defaultStack.getItem().isEnchantable(defaultStack)
							&& EmiAgnos.isEnchantable(defaultStack, e)) {
						consumer.accept(e);
						acceptableEnchantments++;
					}
				}
				if (acceptableEnchantments > 0) {
					for (Enchantment e : universalEnchantments) {
						if (e.canApply(defaultStack)) {
							consumer.accept(e);
							acceptableEnchantments++;
						}
					}
				}
			} catch (Throwable t) {
				EmiReloadLog.warn("Exception thrown registering enchantment recipes", t);
			}
		}
		NonNullList<ItemStack> stacks = NonNullList.create();
		Blocks.DOUBLE_PLANT.getSubBlocks(CreativeTabs.SEARCH, stacks);
		for (ItemStack stack : stacks) {
			if (stack.getItemDamage() != 2 && stack.getItemDamage() != 3) {
				addRecipeSafe(registry, () -> basicWorld(EmiStack.of(stack).setRemainder(EmiStack.of(stack)), EmiStack.of(Items.DYE, 1, 15), EmiStack.of(stack),
					synthetic("world/flower_duping", EmiUtil.subId(EmiPort.id(EmiUtil.subId(stack)))), false));
			}
		}
		addRecipeSafe(registry, () -> new EmiAnvilRecipe(EmiStack.of(Items.SHIELD), EmiStack.ofPotentialTag(new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE)),
			synthetic("anvil/repairing/material", EmiUtil.subId(Items.SHIELD) + "/" + EmiUtil.subId(Blocks.PLANKS))));
	}

	private static void addWorldInteraction(EmiRegistry registry, Set<Item> hiddenItems, List<Item> dyeableItems) {
		EmiStack concreteWater = EmiStack.of(FluidRegistry.WATER);
		concreteWater.setRemainder(concreteWater);
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.WHITE.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.WHITE.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.ORANGE.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.ORANGE.getDyeDamage()));		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.MAGENTA.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.MAGENTA.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.LIGHT_BLUE.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.LIGHT_BLUE.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.YELLOW.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.YELLOW.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.LIME.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.LIME.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.PINK.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.PINK.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.GRAY.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.GRAY.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.SILVER.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.SILVER.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.CYAN.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.CYAN.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.PURPLE.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.PURPLE.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.BLUE.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.BLUE.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.BROWN.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.BROWN.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.GREEN.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.GREEN.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.RED.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.RED.getDyeDamage()));
		addConcreteRecipe(registry, new ItemStack(Blocks.CONCRETE_POWDER, 1, EnumDyeColor.BLACK.getDyeDamage()), concreteWater, new ItemStack(Blocks.CONCRETE, 1, EnumDyeColor.BLACK.getDyeDamage()));

		EmiIngredient hoes = damagedTool(findIngredientByClass(ItemHoe.class, EmiStack.of(Items.IRON_HOE)), 1);
		Map<ItemStack, EmiStack> tillingActions = Map.of(
			new ItemStack(Blocks.DIRT), EmiStack.of(Blocks.FARMLAND),
			new ItemStack(Blocks.GRASS), EmiStack.of(Blocks.FARMLAND),
			new ItemStack(Blocks.DIRT, 1, BlockDirt.DirtType.COARSE_DIRT.getMetadata()), EmiStack.of(Blocks.DIRT)
		);
		for (Map.Entry<ItemStack, EmiStack> entry : tillingActions.entrySet()) {
			ItemStack i = entry.getKey();
			EmiStack o = entry.getValue();
			ResourceLocation id = synthetic("world/tilling", EmiUtil.subId(i));
			addRecipeSafe(registry, () -> basicWorld(EmiStack.of(i), hoes, o, id));
		}

		{
			EmiIngredient shovels = damagedTool(findIngredientByClass(ItemSpade.class, EmiStack.of(Items.IRON_SHOVEL)), 1);
			EmiIngredient grass = EmiStack.of(Blocks.GRASS);
			ResourceLocation id = synthetic("world/flattening", EmiUtil.subId(Blocks.GRASS));
			addRecipeSafe(registry, () -> basicWorld(grass, shovels, EmiStack.of(Blocks.GRASS_PATH), id));
		}

		for (Item i : dyeableItems) {
			if (hiddenItems.contains(i)) {
				continue;
			}
			EmiStack cauldron = EmiStack.of(Items.CAULDRON);
			EmiStack waterThird = EmiStack.of(FluidRegistry.WATER, FluidUnit.BOTTLE);
			int uniq = EmiUtil.RANDOM.nextInt();
			addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
				.id(synthetic("world/cauldron_washing", EmiUtil.subId(i)))
				.leftInput(EmiStack.EMPTY, s -> new GeneratedSlotWidget(r -> {
					ItemStack stack = i.getDefaultInstance();
					((ItemArmor) i).setColor(stack, r.nextInt(0xFFFFFF + 1));
					return EmiStack.of(stack);
				}, uniq, s.getBounds().x(), s.getBounds().y()))
				.rightInput(cauldron, true)
				.rightInput(waterThird, false)
				.output(EmiStack.of(i))
				.supportsRecipeTree(false)
				.build());
		}

		EmiStack water = EmiStack.of(FluidRegistry.WATER, FluidUnit.BUCKET);
		EmiStack lava = EmiStack.of(FluidRegistry.LAVA, FluidUnit.BUCKET);
		EmiStack waterCatalyst = water.copy().setRemainder(water);
		EmiStack lavaCatalyst = lava.copy().setRemainder(lava);

		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_spring", "minecraft/water"))
			.leftInput(waterCatalyst)
			.rightInput(waterCatalyst, false)
			.output(EmiStack.of(FluidRegistry.WATER, FluidUnit.BUCKET))
			.build());
		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_interaction", "minecraft/cobblestone"))
			.leftInput(waterCatalyst)
			.rightInput(lavaCatalyst, false)
			.output(EmiStack.of(Blocks.COBBLESTONE))
			.build());
		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_interaction", "minecraft/stone"))
			.leftInput(waterCatalyst)
			.rightInput(lavaCatalyst, false)
			.output(EmiStack.of(Blocks.STONE))
			.build());
		addRecipeSafe(registry, () -> EmiWorldInteractionRecipe.builder()
			.id(synthetic("world/fluid_interaction", "minecraft/obsidian"))
			.leftInput(lava)
			.rightInput(waterCatalyst, false)
			.output(EmiStack.of(Blocks.OBSIDIAN))
			.build());

		EmiPort.getFluidRegistry().entrySet().forEach(entry -> {
			Fluid fluid = entry.getValue();
			Item bucket = FluidUtil.getFilledBucket(new FluidStack(fluid, 1000)).getItem();
			if (fluid.getStill() != null && !fluid.isGaseous() && bucket != Items.AIR && fluid.canBePlacedInWorld()) {
				addRecipeSafe(registry, () -> basicWorld(EmiStack.of(Items.BUCKET), EmiStack.of(fluid, FluidUnit.BUCKET), EmiStack.of(bucket),
					synthetic("emi", "bucket_filling/" + EmiUtil.subId(fluid)), false));
			}
		});

		addRecipeSafe(registry, () -> basicWorld(EmiStack.of(Items.GLASS_BOTTLE), water,
			EmiStack.of(PotionUtils.addPotionToItemStack(Items.POTIONITEM.getDefaultInstance(), PotionTypes.WATER)),
			synthetic("world/unique", "minecraft/water_bottle")));
	}

	private static EmiIngredient damagedTool(EmiIngredient tool, int damage) {
		for (EmiStack stack : tool.getEmiStacks()) {
			ItemStack is = stack.getItemStack().copy();
			is.setItemDamage(damage);
			stack.setRemainder(EmiStack.of(is));
		}
		return tool;
	}

	private static EmiIngredient getPreferredTag(List<String> candidates, EmiIngredient fallback) {
		for (String id : candidates) {
			EmiIngredient potential = EmiIngredient.of(TagKey.of(ItemKey.class, EmiPort.id(id)));
			if (!potential.isEmpty()) {
				return potential;
			}
		}
		return fallback;
	}

	private static EmiIngredient findIngredientByClass(Class<?> clazz, EmiIngredient fallback) {
		EmiIngredient ingredient = EmiIngredient.of(
			RetroEMI.getAllItems().stream()
				.filter(i -> clazz.isAssignableFrom(i.getClass())).map(EmiStack::of)
				.collect(Collectors.toList()));
		if (!ingredient.isEmpty()) {
			return ingredient;
		}
		return fallback;
	}

	private static void addFuel(EmiRegistry registry, Set<Item> hiddenItems) {
		Map<ItemKey, Integer> fuelMap = EmiAgnos.getFuelMap();
		compressRecipesToTags(fuelMap.keySet().stream().collect(Collectors.toSet()), (a, b) -> {
				return Integer.compare(fuelMap.get(a), fuelMap.get(b));
			}, tag -> {
				EmiIngredient stack = EmiIngredient.of(tag.raw());
				Item item = stack.getEmiStacks().get(0).getItemStack().getItem();
				int time = fuelMap.get(ItemKey.of(stack.getEmiStacks().get(0).getItemStack()));
				registry.addRecipe(new EmiFuelRecipe(stack, time, synthetic("fuel/tag", EmiUtil.subId(tag.id()))));
			}, item -> {
				if (!hiddenItems.contains(item.item())) {
					int time = fuelMap.get(item);
					registry.addRecipe(new EmiFuelRecipe(EmiStack.of(item.toStack()), time, synthetic("fuel/item", EmiUtil.subId(item.toStack()))));
				}
			});
	}

	private static void compressRecipesToTags(Set<ItemKey> stacks, Comparator<ItemKey> comparator, Consumer<EmiTagKey<ItemKey>> tagConsumer, Consumer<ItemKey> itemConsumer) {
		Set<ItemKey> handled = Sets.newHashSet();
		outer:
		for (EmiTagKey<ItemKey> key : (List<EmiTagKey<ItemKey>>) (List<?>) EmiTags.getTags(TagKey.Type.ITEM)) {
			List<ItemKey> items = key.getAll();
			if (items.size() < 2) {
				continue;
			}
			ItemKey base = items.get(0);
			if (!stacks.contains(base)) {
				continue;
			}
			for (int i = 1; i < items.size(); i++) {
				ItemKey item = items.get(i);
				if (!stacks.contains(item) || comparator.compare(base, item) != 0) {
					continue outer;
				}
			}
			if (handled.containsAll(items)) {
				continue;
			}
			handled.addAll(items);
			tagConsumer.accept(key);
		}
		for (ItemKey item : stacks) {
			if (handled.contains(item)) {
				continue;
			}
			itemConsumer.accept(item);
		}
	}

	private static ResourceLocation synthetic(String type, String name) {
		return EmiPort.id("emi", "/" + type + "/" + name);
	}

	private static void safely(String name, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			EmiReloadLog.warn("Exception thrown when reloading " + name  + " step in vanilla EMI plugin", t);
		}
	}

	private static void addRecipeSafe(EmiRegistry registry, Supplier<EmiRecipe> supplier) {
		try {
			registry.addRecipe(supplier.get());
		} catch (Throwable e) {
			EmiReloadLog.warn("Exception thrown when parsing EMI recipe (no ID available)", e);
		}
	}

	private static void addRecipeSafe(EmiRegistry registry, Supplier<EmiRecipe> supplier, IRecipe recipe) {
		try {
			registry.addRecipe(supplier.get());
		} catch (Throwable e) {
			EmiReloadLog.warn("Exception thrown when parsing vanilla recipe " + EmiPort.getId(recipe), e);
		}
	}

	private static EmiRenderable simplifiedRenderer(int u, int v) {
		return (raw, x, y, delta) -> {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			context.drawTexture(EmiRenderHelper.WIDGETS, x, y, u, v, 16, 16);
		};
	}

	private static void addConcreteRecipe(EmiRegistry registry, ItemStack powder, EmiStack water, ItemStack result) {
		addRecipeSafe(registry, () -> basicWorld(EmiStack.of(powder), water, EmiStack.of(result),
			synthetic("world/concrete", EmiUtil.subId(result))));
	}

	private static EmiRecipe basicWorld(EmiIngredient left, EmiIngredient right, EmiStack output, ResourceLocation id) {
		return basicWorld(left, right, output, id, true);
	}

	private static EmiRecipe basicWorld(EmiIngredient left, EmiIngredient right, EmiStack output, ResourceLocation id, boolean catalyst) {
		return EmiWorldInteractionRecipe.builder()
			.id(id)
			.leftInput(left)
			.rightInput(right, catalyst)
			.output(output)
			.build();
	}
}
