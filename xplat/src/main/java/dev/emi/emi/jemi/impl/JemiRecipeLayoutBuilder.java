package dev.emi.emi.jemi.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;

public class JemiRecipeLayoutBuilder implements IRecipeLayout {
	public final List<JemiIngredientAcceptor> ingredients = Lists.newArrayList();
	public final List<JemiRecipeSlotBuilder> slots = Lists.newArrayList();
	public boolean shapeless = false;
	public IRecipeCategory<?> category;

	private final ItemGroup itemStacks;
	private final FluidGroup fluidStacks;
	private final Map<IIngredientType<?>, Group<?>> groups = Maps.newHashMap();
	private final Map<JemiRecipeSlotBuilder, JemiRecipeSlot> slotCache = Maps.newHashMap();

	public JemiRecipeLayoutBuilder() {
		itemStacks = new ItemGroup(this);
		fluidStacks = new FluidGroup(this);
		groups.put(VanillaTypes.ITEM, itemStacks);
		groups.put(VanillaTypes.FLUID, fluidStacks);
	}

	public List<JemiRecipeSlot> getRecipeSlots() {
		List<JemiRecipeSlot> list = Lists.newArrayList();
		for (JemiRecipeSlotBuilder sb : slots) {
			list.add(getSlot(sb));
		}
		return list;
	}

	public JemiRecipeSlot getSlot(JemiRecipeSlotBuilder sb) {
		return slotCache.computeIfAbsent(sb, JemiRecipeSlot::new);
	}

	public void addSlot(RecipeIngredientRole role, int x, int y) {
		JemiRecipeSlotBuilder builder = new JemiRecipeSlotBuilder(role, x, y);
		ingredients.add(builder.acceptor);
		slots.add(builder);
	}

	public void addInvisibleIngredients(RecipeIngredientRole role) {
		JemiIngredientAcceptor acceptor = new JemiIngredientAcceptor(role);
		ingredients.add(acceptor);
	}

	@Override
	public IGuiItemStackGroup getItemStacks() {
		return itemStacks;
	}

	@Override
	public IGuiFluidStackGroup getFluidStacks() {
		return fluidStacks;
	}

	@Override
	public <T> IGuiIngredientGroup<T> getIngredientsGroup(IIngredientType<T> ingredientType) {
		if (ingredientType == VanillaTypes.ITEM) {
			return (IGuiIngredientGroup<T>) (IGuiIngredientGroup<?>) itemStacks;
		} else if (ingredientType == VanillaTypes.FLUID) {
			return (IGuiIngredientGroup<T>) (IGuiIngredientGroup<?>) fluidStacks;
		}
		Group<T> group = (Group<T>) groups.get(ingredientType);
		if (group == null) {
			group = new Group<T>(this, ingredientType);
			groups.put(ingredientType, group);
		}
		return group;
	}

	@Override
	@Deprecated
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> IGuiIngredientGroup<T> getIngredientsGroup(Class<T> ingredientClass) {
		if (JemiIngredients.ingredientRegistry != null) {
			return getIngredientsGroup(JemiIngredients.ingredientRegistry.getIngredientType(ingredientClass));
		}
		return null;
	}

	@Override
	public IFocus<?> getFocus() {
		return null;
	}

	@Override
	public IRecipeCategory<?> getRecipeCategory() {
		return category;
	}

	@Override
	public void setRecipeTransferButton(int posX, int posY) {
	}

	@Override
	public void setShapeless() {
		shapeless = true;
	}

	public static class Group<T> implements IGuiIngredientGroup<T> {
		protected final JemiRecipeLayoutBuilder parent;
		protected final IIngredientType<T> type;
		protected final List<JemiRecipeSlotBuilder> slotBuilders = Lists.newArrayList();
		protected final Map<Integer, JemiRecipeSlotBuilder> slotsByIndex = Maps.newHashMap();
		protected final Set<Integer> inputSlots = Sets.newHashSet();
		protected ITooltipCallback<T> tooltipCallback;

		public Group(JemiRecipeLayoutBuilder parent, IIngredientType<T> type) {
			this.parent = parent;
			this.type = type;
		}

		@Override
		public void init(int slotIndex, boolean input, int x, int y) {
			init(slotIndex, input, null, x, y, 16, 16, 0, 0);
		}

		@Override
		public void init(int slotIndex, boolean input, IIngredientRenderer<T> ingredientRenderer, int x, int y,
				int width, int height, int paddingX, int paddingY) {
			JemiRecipeSlotBuilder builder = new JemiRecipeSlotBuilder(input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT, x, y);
			builder.slotIndex = slotIndex;
			builder.ingredientType = type;
			if (width > 18 || height > 18) {
				builder.large = true;
			}
			builder.width = width;
			builder.height = height;
			builder.paddingX = paddingX;
			builder.paddingY = paddingY;
			if (ingredientRenderer != null) {
				builder.setCustomRenderer(type, ingredientRenderer);
			}
			parent.slots.add(builder);
			parent.ingredients.add(builder.acceptor);
			slotBuilders.add(builder);
			slotsByIndex.put(slotIndex, builder);
			builder.tooltipCallback = tooltipCallback;
			if (input) {
				inputSlots.add(slotIndex);
			}
		}

		@Override
		public void set(IIngredients ingredients) {
			List<List<T>> inputs = ingredients.getInputs(type);
			List<List<T>> outputs = ingredients.getOutputs(type);
			List<Integer> sorted = Lists.newArrayList(slotsByIndex.keySet());
			Collections.sort(sorted);
			int inputIndex = 0, outputIndex = 0;
			for (int index : sorted) {
				if (inputSlots.contains(index)) {
					if (inputIndex < inputs.size()) {
						set(index, inputs.get(inputIndex));
						inputIndex++;
					}
				} else {
					if (outputIndex < outputs.size()) {
						set(index, outputs.get(outputIndex));
						outputIndex++;
					}
				}
			}
		}

		@Override
		public void set(int slotIndex, List<T> ingredients) {
			JemiRecipeSlotBuilder builder = slotsByIndex.get(slotIndex);
			if (builder != null) {
				for (T t : ingredients) {
					builder.acceptor.addIngredient(type, t);
				}
			}
		}

		@Override
		public void set(int slotIndex, T ingredient) {
			JemiRecipeSlotBuilder builder = slotsByIndex.get(slotIndex);
			if (builder != null) {
				builder.acceptor.addIngredient(type, ingredient);
			}
		}

		@Override
		public void setBackground(int slotIndex, IDrawable background) {
			JemiRecipeSlotBuilder builder = slotsByIndex.get(slotIndex);
			if (builder != null) {
				builder.setBackground(background, 0, 0);
			}
		}

		@Override
		public void addTooltipCallback(ITooltipCallback<T> tooltipCallback) {
			this.tooltipCallback = tooltipCallback;
			for (JemiRecipeSlotBuilder builder : slotBuilders) {
				builder.tooltipCallback = tooltipCallback;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public Map<Integer, ? extends IGuiIngredient<T>> getGuiIngredients() {
			Map<Integer, JemiRecipeSlot> map = Maps.newHashMap();
			for (Map.Entry<Integer, JemiRecipeSlotBuilder> entry : slotsByIndex.entrySet()) {
				map.put(entry.getKey(), parent.getSlot(entry.getValue()));
			}
			return (Map<Integer, ? extends IGuiIngredient<T>>) (Map<?, ?>) map;
		}

		@Override
		public void setOverrideDisplayFocus(IFocus<T> focus) {
		}
	}

	public static class ItemGroup extends Group<ItemStack> implements IGuiItemStackGroup {
		public ItemGroup(JemiRecipeLayoutBuilder parent) {
			super(parent, VanillaTypes.ITEM);
		}

		@Override
		public void init(int slotIndex, boolean input, int x, int y) {
			init(slotIndex, input, null, x, y, 18, 18, 1, 1);
		}
	}

	public static class FluidGroup extends Group<FluidStack> implements IGuiFluidStackGroup {
		public FluidGroup(JemiRecipeLayoutBuilder parent) {
			super(parent, VanillaTypes.FLUID);
		}

		@Override
		public void init(int slotIndex, boolean input, int x, int y, int width, int height, int capacityMb, boolean showCapacity, IDrawable overlay) {
			init(slotIndex, input, new FluidStackRenderer(capacityMb, showCapacity, width, height, overlay), x, y, width, height, 0, 0);
			JemiRecipeSlotBuilder builder = slotsByIndex.get(slotIndex);
			if (builder != null) {
				builder.tankInfo = new JemiRecipeSlot.TankInfo(width, height, capacityMb, showCapacity);
				if (overlay != null) {
					builder.overlay = new JemiRecipeSlot.OffsetDrawable(overlay, 0, 0);
				}
			}
		}
	}
}
