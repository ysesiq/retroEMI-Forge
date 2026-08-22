package dev.emi.emi.jemi.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.FluidUnit;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiRecipeSlot.IngredientRenderer;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.oredict.OreDictionary;
import shim.mezz.jei.api.ingredients.ITypedIngredient;
import shim.mezz.jei.api.recipe.RecipeIngredientRole;

public class JemiIngredientAcceptor {
	public static final Pattern FLUID_END = Pattern.compile("(^|\\s)([\\d,]+)\\s*mB$");
	public final RecipeIngredientRole role;
	public final List<EmiStack> stacks = Lists.newArrayList();

	public JemiIngredientAcceptor(RecipeIngredientRole role) {
		this.role = role;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void coerceStacks(ITooltipCallback<?> tooltipCallback, Map<IIngredientType<?>, IngredientRenderer<?>> renderers) {
		if (tooltipCallback == null && renderers == null) {
			return;
		}
		for (EmiStack stack : stacks) {
			ITypedIngredient typed = JemiUtil.getTyped(stack).orElse(null);
			if (typed != null && (stack instanceof JemiStack || stack.getKey() instanceof Fluid)) {
				List<String> base = Lists.newArrayList();
				if (renderers != null && renderers.containsKey(typed.type())) {
					IIngredientRenderer<Object> renderer = (IIngredientRenderer) ((IngredientRenderer) renderers.get(typed.type())).renderer();
					List<String> tooltip = renderer.getTooltip(Minecraft.getMinecraft(), typed.ingredient(), ITooltipFlag.TooltipFlags.NORMAL);
					if (tooltip != null) {
						base.addAll(tooltip);
					}
				}
				if (base.isEmpty()) {
					if (tooltipCallback == null) {
						continue;
					}
					base.add(stack.getName().getString());
					base.add("");
				}
				if (tooltipCallback != null) {
					JemiRecipeSlot jsr = new JemiRecipeSlot(role, stack);
					((ITooltipCallback) tooltipCallback).onTooltip(0, jsr.isInput(), typed.ingredient(), base);
				}
				for (int i = 0; i < 2 && i < base.size(); i++) {
					String t = base.get(i);
					if (t != null) {
						Matcher m = FLUID_END.matcher(t);
						if (m.find()) {
							long amount = Long.parseLong(m.group(2).replace(",", ""));
							if (amount != stack.getAmount()) {
								stack.setAmount(amount);
							}
						}
					}
				}
			}
		}
	}

	public EmiIngredient build() {
		return EmiIngredient.of(stacks);
	}

	private void addStack(EmiStack stack) {
		if (!stack.isEmpty()) {
			ItemStack itemStack = stack.getItemStack();
			if (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
				stacks.addAll(EmiStack.ofPotentialTag(itemStack).getEmiStacks());
			} else {
				stacks.add(stack);
			}
		}
	}

	public <I> void addIngredients(IIngredientType<I> ingredientType, List<I> ingredients) {
		for (I i : ingredients) {
			addIngredient(ingredientType, i);
		}
	}

	public <I> void addIngredient(IIngredientType<I> ingredientType, I ingredient) {
		if (ingredient instanceof List<?> li) {
			for (Object o : li) {
				addStack(JemiUtil.getStack(ingredientType, o));
			}
		} else {
			addStack(JemiUtil.getStack(ingredientType, ingredient));
		}
	}

	public void addIngredientsUnsafe(List<?> ingredients) {
		for (Object o : ingredients) {
			addStack(JemiUtil.getStack(o));
		}
	}

	public void addFluidStack(Fluid fluid) {
		addFluidStack(fluid, FluidUnit.BUCKET);
	}

	public void addFluidStack(Fluid fluid, long amount) {
		addStack(EmiStack.of(fluid, amount));
	}

	public void addFluidStack(Fluid fluid, long amount, NBTTagCompound componentChanges) {
		addStack(EmiStack.of(fluid, componentChanges, amount));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addTypedIngredients(List<ITypedIngredient<?>> ingredients) {
		for (ITypedIngredient<?> i : ingredients) {
			addIngredient(((IIngredientType) i.type()), i.ingredient());
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void addOptionalTypedIngredients(List<Optional<ITypedIngredient<?>>> ingredients) {
		for (Optional<ITypedIngredient<?>> opt : ingredients) {
			if (opt.isPresent()) {
				ITypedIngredient<?> i = opt.get();
				addIngredient(((IIngredientType) i.type()), i.ingredient());
			}
		}
	}
}
