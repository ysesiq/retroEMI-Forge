package dev.emi.emi.jemi;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiIngredientAcceptor;
import dev.emi.emi.jemi.impl.JemiIngredients;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.jemi.impl.JemiRecipeSlotBuilder;
import dev.emi.emi.runtime.ProxyRecipeManager;
import dev.emi.emi.screen.WidgetGroup;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.renderer.GlStateManager;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;
import dev.emi.emi.jemi.widget.JemiSlotWidget;
import dev.emi.emi.jemi.widget.JemiTankWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JemiRecipe<T extends IRecipeWrapper> implements EmiRecipe {
	public List<EmiIngredient> inputs = Lists.newArrayList();
	public List<EmiIngredient> catalysts = Lists.newArrayList();
	public List<EmiStack> outputs = Lists.newArrayList();
	public EmiRecipeCategory recipeCategory;
	public ResourceLocation originalId, id;
	public IRecipeCategory<T> category;
	public T recipe;
	public boolean allowTree = true;

	public JemiRecipe(EmiRecipeCategory recipeCategory, IRecipeCategory<T> category, T recipe) {
		this.recipeCategory = recipeCategory;
		this.category = category;
		this.recipe = recipe;
		this.originalId = recipe instanceof ICraftingRecipeWrapper crafting ? crafting.getRegistryName() : null;
		JemiIngredients ingredients = new JemiIngredients();
		recipe.getIngredients(ingredients);
		JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
		builder.category = category;
		try {
			category.setRecipe(builder, recipe, ingredients);
		} catch (Throwable ignored) {
		}
		for (JemiRecipeSlotBuilder jrsb : builder.slots) {
			jrsb.acceptor.coerceStacks(jrsb.tooltipCallback, jrsb.renderers);
		}
		for (JemiIngredientAcceptor acceptor : builder.ingredients) {
			EmiIngredient stack = acceptor.build();
			if (acceptor.role == RecipeIngredientRole.INPUT) {
				inputs.add(stack);
			} else if (acceptor.role == RecipeIngredientRole.CATALYST) {
				catalysts.add(stack);
			} else if (acceptor.role == RecipeIngredientRole.OUTPUT) {
				if (stack.getEmiStacks().size() > 1) {
					allowTree = false;
				}
				outputs.addAll(stack.getEmiStacks());
			}
		}
		if (this.originalId != null) {
			this.id = EmiPort.id("jei", "/" + EmiUtil.subId(this.originalId));
		} else {
			this.id = EmiPort.id("jei", "/" + getContentId());
		}
	}

	private String getContentId() {
		StringBuilder sb = new StringBuilder(category.getUid().replace('.', '/'));
		for (List<? extends EmiIngredient> ingredients : Arrays.asList(inputs, catalysts, outputs)) {
			for (EmiIngredient ingredient : ingredients) {
				sb.append('/');
				String ids = ingredient.getEmiStacks().stream()
					.filter(s -> !s.isEmpty() && s.getId() != null)
					.map(EmiUtil::subId)
					.map(rl -> rl.replace(':', '.').replaceAll("[^a-z0-9/._-]", "_"))
					.distinct()
					.collect(Collectors.joining("+"));
				sb.append(ids.isEmpty() ? "empty" : ids);
			}
		}
		return sb.toString();
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return recipeCategory;
	}

	@Override
	public @Nullable IRecipe getBackingRecipe() {
		return ProxyRecipeManager.getRecipeEntry(originalId);
	}

	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getDisplayWidth() {
		return category.getBackground().getWidth();
	}

	@Override
	public int getDisplayHeight() {
		return category.getBackground().getHeight();
	}

	@Override
	public boolean supportsRecipeTree() {
		return allowTree && EmiRecipe.super.supportsRecipeTree();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addWidgets(WidgetHolder widgets) {
		int posX = 0, posY = 0;
		if (widgets instanceof WidgetGroup group) {
			posX = group.x;
			posY = group.y;
		}
		JemiIngredients ingredients = new JemiIngredients();
		recipe.getIngredients(ingredients);
		JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
		builder.category = category;
		try {
			category.setRecipe(builder, recipe, ingredients);
		} catch (Throwable ignored) {
		}
		for (JemiRecipeSlotBuilder jrsb : builder.slots) {
			jrsb.acceptor.coerceStacks(jrsb.tooltipCallback, jrsb.renderers);
		}
		Optional<IRecipeLayoutDrawable> opt = Optional.ofNullable(RecipeLayout.create(-1, category, recipe, null, posX, posY));
		if (opt.isPresent()) {
			widgets.add(new JemiWidget(0, 0, getDisplayWidth(), getDisplayHeight(), opt.get()));
			for (JemiRecipeSlotBuilder sb : builder.slots) {
				JemiRecipeSlot slot = new JemiRecipeSlot(sb);
				slot.layout = opt.get();
				if ((slot.tankInfo != null && !slot.getIngredients(JemiUtil.getFluidType()).collect(Collectors.toList()).isEmpty())) {
					widgets.add(new JemiTankWidget(slot, this));
				} else {
					widgets.add(new JemiSlotWidget(slot, this));
				}
			}
		}
//		try {
//			JemiRecipeExtrasBuilder extras = new JemiRecipeExtrasBuilder(null);
//			category.createRecipeExtras(extras, recipe, JemiPlugin.runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
//			for (JemiWidgetBuilder b : extras.widgets) {
//				b.addWidgets(widgets);
//			}
//		} catch(Throwable t) {
//			EmiLog.error("Exception adding JEMI extras", t);
//		}
	}

	public class JemiWidget extends Widget {

		private final IRecipeLayoutDrawable recipeLayoutDrawable;
		private final Bounds bounds;
		private final int x, y;

		public JemiWidget(int x, int y, int w, int h, IRecipeLayoutDrawable recipeLayoutDrawable) {
			this.recipeLayoutDrawable = recipeLayoutDrawable;
			this.bounds = new Bounds(x, y, w, h);
			this.x = x;
			this.y = y;
		}

		@Override
		public Bounds getBounds() {
			return bounds;
		}

		@Override
		public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
			EmiDrawContext context = EmiDrawContext.wrap(draw);
			Minecraft client = Minecraft.getMinecraft();
			context.push();
			GlStateManager.disableLighting();
			GlStateManager.enableAlpha();
			context.translate(x, y);
			IDrawable background = category.getBackground();
			if (background != null) {
				background.draw(client, 0, 0);
			}
			category.drawExtras(client);
			recipe.drawInfo(client, getDisplayWidth(), getDisplayHeight(), mouseX, mouseY);
			context.disableBlend();
			GlStateManager.disableLighting();
			GlStateManager.disableAlpha();
			context.resetColor();
			context.pop();
		}

		@Override
		public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
			List<TooltipComponent> list = Lists.newArrayList();
			list.addAll(Stream.of(
					Optional.ofNullable(category.getTooltipStrings(mouseX, mouseY)).orElse(shim.java.List.of()),
					Optional.ofNullable(recipe.getTooltipStrings(mouseX, mouseY)).orElse(shim.java.List.of()))
					.flatMap(List::stream).map(s -> TooltipComponent.of(EmiPort.literal(s))).collect(Collectors.toList()));
			return list;
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			return recipe.handleClick(Minecraft.getMinecraft(), mouseX, mouseY, button);
		}

//		@Override
//		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//			return recipe.handleInput(recipe, EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, InputUtil.fromKeyCode(keyCode, scanCode));
//		}
	}
}
