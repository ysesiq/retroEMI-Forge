package dev.emi.emi.jemi;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.jemi.impl.JemiIngredients;
import dev.emi.emi.jemi.runtime.JemiDragDropHandler;
import dev.emi.emi.mixin.jei.accessor.IngredientFilterAccessor;
import dev.emi.emi.mixin.jei.accessor.IngredientListOverlayAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.registry.EmiRecipes;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.runtime.EmiReloadManager;
import mezz.jei.Internal;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.input.IClickedIngredient;
import mezz.jei.plugins.jei.info.IngredientInfoRecipe;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.net.minecraft.text.Text;

@JEIPlugin
@EmiEntrypoint
public class JemiPlugin implements IModPlugin, EmiPlugin {
	static final Map<EmiRecipeCategory, IRecipeCategory<?>> CATEGORY_MAP = Maps.newHashMap();
	private static ISubtypeRegistry subtypeRegistry;
	public static IJeiRuntime runtime;
//	public static BiPredicate<IIngredientTypeWithSubtypes<? extends Object, ? extends Object>, Object> hasSubtype = (a, b) -> true;
	public static IIngredientRegistry ingredientRegistry;

//	@Override
//	public ResourceLocation getPluginUid() {
//		return EmiPort.id("emi:jemi");
//	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry registration) {
		subtypeRegistry = registration;
	}

	@Override
	public void register(IModRegistry registry) {
		ingredientRegistry = registry.getIngredientRegistry();
		JemiIngredients.ingredientRegistry = ingredientRegistry;
	}

//    @Override
//    public void registerRuntime(IRuntimeRegistration registration) {
//        registration.setIngredientListOverlay(new JemiIngredientListOverlay());
//        registration.setBookmarkOverlay(new JemiBookmarkOverlay());
//        registration.setRecipesGui(new JemiRecipesGui());
//        registration.setIngredientFilter(new JemiIngredientFilter());
//    }

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		JemiPlugin.runtime = runtime;
	}

//	@Override
//	public void onRuntimeUnavailable() {
//		JemiPlugin.runtime = null;
//	}

	public static boolean isIngredientHidden(ITypedIngredient<?> typed) {
		try {
			if (Internal.getIngredientFilter() instanceof IngredientFilterAccessor acc) {
				IIngredientHelper helper = ingredientRegistry.getIngredientHelper(typed.type());
				return acc.getBlacklist().isIngredientBlacklisted(typed.ingredient(), helper);
			}
		} catch (Throwable t) {
			return false;
		}
		return false;
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void register(EmiRegistry registry) {
		EmiLog.info("[JEMI] Waiting for JEI to finish reloading...");
		EmiReloadManager.step(EmiPort.literal("Waiting for JEI to finish..."), 20_000);
		try {
			while (true) {
				if (runtime != null) {
					break;
				}
				Thread.sleep(100);
			}
		} catch (Exception e) {
			return;
		}
		EmiLog.info("[JEMI] JEI reloaded!");
		Set<String> handledNamespaces = EmiAgnos.getPlugins().stream().map(EmiPluginContainer::id).collect(Collectors.toSet());

		EmiReloadManager.step(EmiPort.literal("Loading information from JEI..."), 5_000);
		registry.addGenericExclusionArea((screen, consumer) -> {
			if (runtime != null && ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).getGuiScreenHelper() != null) {
				Set<Rectangle> areas = ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).getGuiScreenHelper().getGuiExclusionAreas();
				for (Rectangle r : areas) {
					if (r != null) {
						consumer.accept(Bounds.ofRectangle(r));
					}
				}
			}
			if (screen instanceof GuiContainer) {
				GuiIconButton configButton = JemiUtil.getConfigButton(runtime);
				if (configButton.visible) {
					consumer.accept(new Bounds(configButton.x, configButton.y, configButton.width, configButton.height));
				}
				GuiIconButton bookmarkButton = JemiUtil.getBookmarkButton(runtime);
				if (bookmarkButton.visible) {
					consumer.accept(new Bounds(bookmarkButton.x, bookmarkButton.y, bookmarkButton.width, bookmarkButton.height));
				}
			}
		});

		registry.addGenericStackProvider((screen, x, y) -> {
			EmiStack stack = EmiStack.EMPTY;
			GuiScreenHelper helper = ((IngredientListOverlayAccessor) runtime.getIngredientListOverlay()).getGuiScreenHelper();
			if (helper != null && screen instanceof GuiContainer) {
				IClickedIngredient<?> clicked = helper.getPluginsIngredientUnderMouse((GuiContainer) screen, x, y);
				if (clicked != null) {
					stack = JemiUtil.getStack(clicked.getValue());
				}
			}
			return new EmiStackInteraction(stack, null, false);
		});

		registry.addGenericDragDropHandler(new JemiDragDropHandler());

		registry.addIngredientSerializer(JemiStack.class, new JemiStackSerializer(ingredientRegistry));

		EmiReloadManager.step(EmiPort.literal("Processing JEI stacks..."), 5_000);
		for (IIngredientType<?> type : ingredientRegistry.getRegisteredIngredientTypes()) {
			if (type == JemiUtil.getFluidType() || type == VanillaTypes.ITEM) {
				continue;
			}
			for (Object o : ingredientRegistry.getAllIngredients(type)) {
				EmiStack stack = JemiUtil.getStack(type, o);
				if (!stack.isEmpty()) {
					registry.addEmiStack(stack);
				}
			}
		}

		registry.removeEmiStacks(s -> {
			try {
				Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(s);
				if (opt.isPresent()) {
					return isIngredientHidden(opt.get());
				}
			} catch (Throwable t) {
			}
			return false;
		});
		EmiReloadManager.step(EmiPort.literal("Processing JEI subtypes..."), 5_000);
		safely("subtype comparison", () -> parseSubtypes(registry));

		EmiReloadManager.step(EmiPort.literal("Processing JEI recipes..."), 5_000);
		Set<ResourceLocation> existingCategories = EmiRecipes.categories.stream().map(EmiRecipeCategory::getId).collect(Collectors.toSet());
		Map<String, EmiRecipeCategory> categoryMap = Maps.newHashMap();
		categoryMap.put(VanillaRecipeCategoryUid.CRAFTING, VanillaEmiRecipeCategories.CRAFTING);
		categoryMap.put(VanillaRecipeCategoryUid.SMELTING, VanillaEmiRecipeCategories.SMELTING);
		categoryMap.put(VanillaRecipeCategoryUid.ANVIL, VanillaEmiRecipeCategories.ANVIL_REPAIRING);
		categoryMap.put(VanillaRecipeCategoryUid.BREWING, VanillaEmiRecipeCategories.BREWING);
		categoryMap.put(VanillaRecipeCategoryUid.FUEL, VanillaEmiRecipeCategories.FUEL);
		categoryMap.put(VanillaRecipeCategoryUid.INFORMATION, VanillaEmiRecipeCategories.INFO);
		categoryMap.put(VanillaRecipeCategoryUid.DESCRIPTION, VanillaEmiRecipeCategories.INFO);

		CATEGORY_MAP.clear();
		EmiRecipeFiller.extraHandlers = JemiPlugin::getRecipeHandler;

		List<IRecipeCategory> categories = runtime.getRecipeRegistry().getRecipeCategories();
		for (IRecipeCategory c : categories) {
			EmiLog.info("[JEMI] Collecting data for " + c.getTitle());
			EmiReloadManager.step(EmiPort.literal("Loading JEI data for " + c.getTitle()), 5_000);
			try {
//				RecipeType type = c.getRecipeType();
				ResourceLocation id = EmiPort.id(c.getUid());
				List<EmiStack> catalysts = runtime.getRecipeRegistry().getRecipeCatalysts(c).stream().map(JemiUtil::getStack).collect(Collectors.toList());
				if (categoryMap.containsKey(c.getUid())) {
					EmiRecipeCategory category = categoryMap.get(c.getUid());
					CATEGORY_MAP.put(category, c);
					for (EmiStack catalyst : catalysts) {
						if (!catalyst.isEmpty()) {
							registry.addWorkstation(category, catalyst);
						}
					}
					if (category == VanillaEmiRecipeCategories.INFO) {
						addInfoRecipes(registry, c);
//					} else if (type == RecipeTypes.CRAFTING) {
//						addCraftingRecipes(registry, (IRecipeCategory<RecipeEntry<CraftingRecipe>>) c);
					}
					continue;
				}
				if (handledNamespaces.contains(id.getNamespace())) {
					EmiLog.info("[JEMI] Skipping recipe category " + id + " because mod is already handled");
					continue;
				}
				if (existingCategories.contains(id)) {
					EmiLog.info("[JEMI] Skipping recipe category " + id + " because native EMI recipe category already exists");
					continue;
				}
				EmiRecipeCategory category = new JemiCategory(c);
				CATEGORY_MAP.put(category, c);
				registry.addCategory(category);
				for (EmiStack catalyst : catalysts) {
					if (!catalyst.isEmpty()) {
						registry.addWorkstation(category, catalyst);
					}
				}
				List<IRecipeWrapper> recipes = runtime.getRecipeRegistry().getRecipeWrappers(c);
				for (IRecipeWrapper r : recipes) {
					try {
						registry.addRecipe(new JemiRecipe(category, c, r));
					} catch (Throwable t) {
						EmiLog.error("Exception thrown adding adding JEI recipe", t);
					}
				}
			} catch(Throwable t) {
				EmiLog.error("Exception thrown adding adding JEI recipes", t);
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void addInfoRecipes(EmiRegistry registry, IRecipeCategory category) {
		Map<List<EmiStack>, List<Text>> grouped = Maps.newHashMap();
		for (IRecipeWrapper recipe : (List<IRecipeWrapper>) (List<?>) runtime.getRecipeRegistry().getRecipeWrappers(category)) {
			JemiIngredients ingredients = new JemiIngredients();
			recipe.getIngredients(ingredients);
			List<EmiStack> stacks = Lists.newArrayList();
			for (IIngredientType<?> type : ingredientRegistry.getRegisteredIngredientTypes()) {
				for (List<?> slot : ingredients.getInputs(type)) {
					for (Object o : slot) {
						EmiStack stack = JemiUtil.getStack(type, o);
						if (!stack.isEmpty()) {
							stacks.add(stack);
						}
					}
				}
			}
			if (stacks.isEmpty() || !(recipe instanceof IngredientInfoRecipe<?> info)) {
				continue;
			}
			List<Text> lines = grouped.computeIfAbsent(stacks, k -> Lists.newArrayList());
			for (String line : info.getDescription()) {
				if (!line.isEmpty()) {
					lines.add(EmiPort.literal(line));
				}
			}
		}
		for (Map.Entry<List<EmiStack>, List<Text>> group : grouped.entrySet()) {
			List<EmiStack> stacks = group.getKey();
			registry.addRecipe(new EmiInfoRecipe(stacks.stream().map(s -> (EmiIngredient) s).collect(Collectors.toList()), group.getValue(), EmiPort.id("jemi", "/info/" + EmiUtil.subId(stacks.get(0)))));
		}
	}

//	private void addCraftingRecipes(EmiRegistry registry, IRecipeCategory<RecipeEntry<CraftingRecipe>> category) {
//		Set<Identifier> replaced = Sets.newHashSet();
//		Set<EmiRecipe> replacements = Sets.newHashSet();
//		List<RecipeEntry<CraftingRecipe>> recipes = Stream.concat(
//			runtime.getRecipeManager().createRecipeLookup(category.getRecipeType()).includeHidden().get(),
//			registry.getRecipeManager().listAllOfType(net.minecraft.recipe.RecipeType.CRAFTING).stream()
//				.filter(r -> r.value() instanceof SpecialCraftingRecipe)
//		).distinct().toList();
//		for (RecipeEntry<CraftingRecipe> recipe : recipes) {
//			try {
//				if (category.isHandled(recipe)) {
//					JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
//					category.setRecipe(builder, recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
//					List<EmiIngredient> inputs = Lists.newArrayList();
//					List<EmiStack> outputs = Lists.newArrayList();
//					for (JemiIngredientAcceptor acceptor : builder.ingredients) {
//						EmiIngredient stack = acceptor.build();
//						if (acceptor.role == RecipeIngredientRole.INPUT) {
//							inputs.add(stack);
//						} else if (acceptor.role == RecipeIngredientRole.CATALYST) {
//							inputs.add(stack);
//						} else if (acceptor.role == RecipeIngredientRole.OUTPUT) {
//							outputs.addAll(stack.getEmiStacks());
//						}
//					}
//					if (inputs.stream().anyMatch(i -> !i.isEmpty()) && outputs.stream().anyMatch(o -> !o.isEmpty())) {
//						EmiRecipe replacement;
//						if (outputs.size() > 1) {
//							replacement = new EmiPatternCraftingRecipe(inputs, EmiStack.EMPTY, category.getRegistryName(recipe), builder.shapeless) {
//
//								@Override
//								public List<EmiStack> getOutputs() {
//									return outputs;
//								}
//
//								@Override
//								public SlotWidget getInputWidget(int slot, int x, int y) {
//									if (slot <= inputs.size()) {
//										return new SlotWidget(inputs.get(slot), x, y);
//									} else {
//										return new SlotWidget(EmiStack.EMPTY, x, y);
//									}
//								}
//
//								@Override
//								public SlotWidget getOutputWidget(int x, int y) {
//									return new GeneratedSlotWidget(r -> outputs.get(r.nextInt(outputs.size())), recipe.hashCode(), x, y);
//								}
//
//							};
//						} else {
//							replacement = new EmiCraftingRecipe(inputs, outputs.get(0), category.getRegistryName(recipe), builder.shapeless);
//						}
//						if (replacement.getId() != null) {
//							replaced.add(replacement.getId());
//						}
//						replacements.add(replacement);
//						registry.addRecipe(replacement);
//					}
//				}
//			} catch (Throwable t) {
//				EmiLog.error("[JEMI] Exception thrown setting JEI crafting recipe", t);
//			}
//		}
//		registry.removeRecipes(r -> r instanceof EmiCraftingRecipe && replaced.contains(r.getId()) && !replacements.contains(r));
//	}

	@SuppressWarnings({"unchecked"})
	private void parseSubtypes(EmiRegistry registry) {
		if (subtypeRegistry != null) {
//			IIngredientManager im = runtime.getIngredientManager();
//			List<IIngredientType<?>> types = Lists.newArrayList(im.getRegisteredIngredientTypes());
			for (Item item : EmiPort.getItemRegistry()) {
				ItemStack defaultStack = item.getDefaultInstance();
				if (defaultStack.isEmpty()) continue;
				if (subtypeRegistry.hasSubtypeInterpreter(defaultStack)) {
					registry.setDefaultComparison(item, Comparison.compareData(stack -> {
						return subtypeRegistry.getSubtypeInfo(stack.getItemStack());
					}));
				}
			}
			for (Fluid fluid : EmiPort.getFluidRegistry().values()) {
				if (subtypeRegistry.hasSubtypeInterpreter(new FluidStack(fluid, 1000))) {
					registry.setDefaultComparison(fluid, Comparison.compareData(stack -> {
						if (stack.getKey() instanceof Fluid f) {
							return subtypeRegistry.getSubtypeInfo(new FluidStack(f, 1000));
						}
						return null;
					}));
				}
			}
//			for (IIngredientType<?> type : types) {
//				if (type == VanillaTypes.ITEM_STACK || type == JemiUtil.getFluidType()) {
//					continue;
//				}
//				if (type instanceof IIngredientTypeWithSubtypes iitws) {
//					List<Object> ings = Lists.newArrayList(im.getAllIngredients(type));
//					for (Object o : ings) {
//						try {
//							if (hasSubtype.test(iitws, o)) {
//								registry.setDefaultComparison(iitws.getBase(o), Comparison.compareData(stack -> {
//									if (stack instanceof JemiStack jemi) {
//										return subtypeRegistry.getSubtypeInfo(iitws, jemi.ingredient, UidContext.Recipe);
//									}
//									return null;
//								}));
//							}
//						} catch (Throwable t) {
//							EmiReloadLog.warn("Exception adding default comparison for JEI ingredient", t);
//						}
//					}
//				}
//			}
		}
	}

	private static EmiRecipeHandler<?> getRecipeHandler(Container handler, EmiRecipe recipe) {
		IRecipeCategory<?> category = CATEGORY_MAP.getOrDefault(recipe.getCategory(), null);
		if (category != null) {
			IRecipeTransferHandler transferHandler = runtime.getRecipeRegistry().getRecipeTransferHandler(handler, category);
			if (transferHandler != null) {
				return new JemiRecipeHandler<>(transferHandler);
			}
		}
		return null;
	}

	private static void safely(String name, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			EmiReloadLog.warn("Exception thrown when reloading " + name  + " step in JEMI plugin", t);
		}
	}
}
