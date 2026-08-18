package dev.emi.emi.jemi;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.jemi.impl.JemiIngredients;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.impl.JemiRecipeSlotBuilder;
import dev.emi.emi.runtime.ProxyRecipeManager;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import shim.net.minecraft.client.gui.DrawContext;

public class JemiRecipeHandler<T extends Container> implements EmiRecipeHandler<T> {
//	private final RecipeType type;
	//private IRecipeCategory<R> category;
	public IRecipeTransferHandler handler;

	public JemiRecipeHandler(IRecipeTransferHandler handler) {
		this.handler = handler;
//		type = handler.getRecipeType();
		/*
		if (type != null) {
			List<IRecipeCategory<R>> categories = (List<IRecipeCategory<R>>) (Object) JemiPlugin.runtime.getRecipeManager().createRecipeCategoryLookup().includeHidden().limitTypes(List.of(type)).get().toList();
			if (!categories.isEmpty()) {
				category = categories.get(0);
			}
		}*/
	}

	@Override
	public boolean alwaysDisplaySupport(EmiRecipe recipe) {
		return false;
	}

	@Override
	public EmiPlayerInventory getInventory(GuiContainer screen) {
		return new EmiPlayerInventory(shim.java.List.of());
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return (recipe instanceof JemiRecipe) && recipe.supportsRecipeTree();
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<T> context) {
		IRecipeTransferError err = jeiCraft(recipe, context, false, null);
		return err == null || err.getType() == IRecipeTransferError.Type.USER_FACING;
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<T> context) {
		IRecipeTransferError err = jeiCraft(recipe, context, true, null);
		if (err == null || err.getType() == IRecipeTransferError.Type.USER_FACING) {
			Minecraft.getMinecraft().displayGuiScreen(context.getScreen());
		}
		return err == null || err.getType() == IRecipeTransferError.Type.USER_FACING;
	}

	@Override
	public void render(EmiRecipe recipe, EmiCraftContext<T> context, List<Widget> widgets, DrawContext raw) {
		EmiDrawContext draw = EmiDrawContext.wrap(raw);
		JemiRecipeLayoutBuilder view = createSlotsView(recipe, widgets);
		IRecipeTransferError err = jeiCraft(recipe, context, false, view);
		if (err != null && view != null) {
			view.getRecipeSlots().forEach(v -> v.highlight = 0);
			draw.push();
			draw.matrices().translate(-100000, -100000, -100000);
			draw.matrices().scale(0, 0, 0);
			err.showError(Minecraft.getMinecraft(), EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, view, 0, 0);
			draw.pop();
			view.getRecipeSlots().forEach(v -> {
				if (v.highlight != 0 && !v.isEmpty()) {
					draw.fill(v.x, v.y, 18, 18, v.highlight);
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	private IRecipeTransferError jeiCraft(EmiRecipe recipe, EmiCraftContext<T> context, boolean craft, JemiRecipeLayoutBuilder view) {
		try {
			Minecraft client = Minecraft.getMinecraft();

			if (view == null) {
				view = createSlotsView(recipe, Collections.emptyList());
			}

			if (view == null) {
				return new InternalRecipeTransferError();
			}

			return handler.transferRecipe(context.getScreenHandler(), view, client.player, context.getAmount() > 1, craft);
		} catch (Exception e) {
			EmiLog.error("Error executing JEI craft", e);
		}
		return new InternalRecipeTransferError();
	}

	private JemiRecipeLayoutBuilder createSlotsView(EmiRecipe recipe, List<Widget> widgets) {
		JemiRecipeLayoutBuilder builder = null;
		if (recipe instanceof JemiRecipe jr && jr.category != null) {
			builder = new JemiRecipeLayoutBuilder();
			builder.category = jr.category;
			JemiIngredients ingredients = new JemiIngredients();
			jr.recipe.getIngredients(ingredients);
			jr.category.setRecipe(builder, jr.recipe, ingredients);
		}

		if (builder == null) {
			List<SlotWidget> slotWidgets = widgets.stream().filter(w -> w instanceof SlotWidget).map(w -> (SlotWidget) w).collect(Collectors.toList());
			builder = new JemiRecipeLayoutBuilder();
			addIngredients(builder, slotWidgets, recipe.getOutputs(), RecipeIngredientRole.OUTPUT);
			int blankedSlots = 0;
			// People assume very specific slot layouts from JEI. Oblige them.
			if (recipe instanceof EmiCraftingRecipe ecr) {
				if (ecr.shapeless) {
					int inputSize = recipe.getInputs().size();
					if (inputSize == 1) {
						addBlankIngredients(builder, slotWidgets, 4, RecipeIngredientRole.INPUT);
						blankedSlots += 4;
						addIngredients(builder, slotWidgets, recipe.getInputs(), RecipeIngredientRole.INPUT);
					} else if (inputSize < 5) {
						int wrap = 0;
						for (EmiIngredient i : recipe.getInputs()) {
							addIngredients(builder, slotWidgets, Collections.singletonList(i), RecipeIngredientRole.INPUT);
							wrap++;
							if (wrap >= 2) {
								wrap = 0;
								addBlankIngredients(builder, slotWidgets, 1, RecipeIngredientRole.INPUT);
								blankedSlots += 1;
							}
						}
					} else {
						addIngredients(builder, slotWidgets, recipe.getInputs(), RecipeIngredientRole.INPUT);
					}
				} else {
					if (ecr.canFit(1, 3)) {
						addBlankIngredients(builder, slotWidgets, 1, RecipeIngredientRole.INPUT);
						blankedSlots += 1;
					} else if (ecr.canFit(3, 1) || (ecr.canFit(3, 2) && !ecr.canFit(2, 2))) {
						addBlankIngredients(builder, slotWidgets, 3, RecipeIngredientRole.INPUT);
						blankedSlots += 3;
					}
					addIngredients(builder, slotWidgets, recipe.getInputs().subList(0, Math.max(9, recipe.getInputs().size()) - blankedSlots), RecipeIngredientRole.INPUT);
				}
			} else {
				addIngredients(builder, slotWidgets, recipe.getInputs(), RecipeIngredientRole.INPUT);
			}
			if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING) {
				for (int i = recipe.getInputs().size() + blankedSlots; i < 9; i++) {
					addIngredients(builder, slotWidgets, Collections.singletonList(EmiStack.EMPTY), RecipeIngredientRole.INPUT);
				}
			}
			addIngredients(builder, slotWidgets, recipe.getCatalysts(), RecipeIngredientRole.CATALYST);
		}

		return builder;
	}

//	@SuppressWarnings("unchecked")
//	private R getRawRecipe(EmiRecipe recipe) {
//		try {
//			if (type != null && type.getRecipeClass() != null) {
//				if (recipe instanceof JemiRecipe jr && jr.recipe != null) {
//					if (type.getRecipeClass().isAssignableFrom(jr.recipe.getClass())) {
//						return type.getRecipeClass().cast(jr.recipe);
//					}
//				}
//				RecipeEntry<?> entry = ProxyRecipeManager.getRecipeEntry(recipe.getId());
//				if (entry != null && type.getRecipeClass().isAssignableFrom(entry.getClass())) {
//					return type.getRecipeClass().cast(entry);
//				}
//			}
//			RecipeEntry<?> entry = ProxyRecipeManager.getRecipeEntry(recipe.getId());
//			return (R) entry;
//		} catch (Exception e) {
//		}
//		return null;
//	}

	private static void addBlankIngredients(JemiRecipeLayoutBuilder builder, List<SlotWidget> widgets, int amount, RecipeIngredientRole role) {
		for (int i = 0; i < amount; i++) {
			addIngredients(builder, widgets, shim.java.List.of(EmiStack.EMPTY), RecipeIngredientRole.INPUT);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void addIngredients(JemiRecipeLayoutBuilder builder, List<SlotWidget> widgets, List<? extends EmiIngredient> stacks, RecipeIngredientRole role) {
		for (EmiIngredient ing : stacks) {
			int x = 0, y = 0;
			for (SlotWidget w : widgets) {
				if (w.getStack() == ing) {
					x = w.getBounds().x();
					y = w.getBounds().y();
				}
			}
			builder.addSlot(role, x, y);
			JemiRecipeSlotBuilder sb = builder.slots.get(builder.slots.size() - 1);
			for (EmiStack stack : ing.getEmiStacks()) {
				Optional<ITypedIngredient<?>> opt = JemiUtil.getTyped(stack);
				if (opt.isPresent()) {
					ITypedIngredient<?> typed = opt.get();
					sb.acceptor.addIngredient((IIngredientType) typed.type(), typed.ingredient());
				}
			}
		}
	}

	private static class InternalRecipeTransferError implements IRecipeTransferError {

		@Override
		public Type getType() {
			return Type.INTERNAL;
		}

		@Override
		public void showError(Minecraft mc, int mouseX, int mouseY, IRecipeLayout layout, int xOffset, int yOffset) {
			EmiLog.error("JEI recipe transfer error");
		}
	}
}
