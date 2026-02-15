package dev.emi.emi.api.stack;

import com.google.common.collect.Lists;
import com.rewindmc.retroemi.Prototype;
import dev.emi.emi.EmiPort;
import dev.emi.emi.registry.EmiComparisonDefaults;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import com.rewindmc.retroemi.ItemStacks;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.item.DyeItem;
import shim.net.minecraft.text.Text;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An abstract representation of a resource in EMI.
 * Can be an item, a fluid, or something else.
 */
public abstract class EmiStack implements EmiIngredient {
	public static final EmiStack EMPTY = new EmptyEmiStack();
	private EmiStack remainder = EMPTY;
	protected Comparison comparison = Comparison.DEFAULT_COMPARISON;
	protected long amount = 1;
	protected float chance = 1;

    @Override
	public List<EmiStack> getEmiStacks() {
		return shim.java.List.of(this);
	}

	public EmiStack getRemainder() {
		return remainder;
	}

	public EmiStack setRemainder(EmiStack stack) {
		if (stack == this) {
			stack = stack.copy();
		}
		remainder = stack;
		return this;
	}

	public EmiStack comparison(Function<Comparison, Comparison> comparison) {
		this.comparison = comparison.apply(this.comparison);
		return this;
	}

	public EmiStack comparison(Comparison comparison) {
		this.comparison = comparison;
		return this;
	}

	public abstract EmiStack copy();

	public abstract boolean isEmpty();

	public long getAmount() {
		return amount;
	}

	public EmiStack setAmount(long amount) {
		this.amount = amount;
		return this;
	}

	public float getChance() {
		return chance;
	}

	public EmiStack setChance(float chance) {
		this.chance = chance;
		return this;
	}

	public abstract NBTTagCompound getNbt();

	public boolean hasNbt() {
		return getNbt() != null;
	}

	public abstract Object getKey();

	@SuppressWarnings("unchecked")
	public <T> @Nullable T getKeyOfType(Class<T> clazz) {
		Object o = getKey();
		if (clazz.isAssignableFrom(o.getClass())) {
			return (T) o;
		}
		return null;
	}

	public abstract ResourceLocation getId();

	public ItemStack getItemStack() {
		return ItemStacks.EMPTY;
	}

	public boolean isEqual(EmiStack stack) {
		if (!getKey().equals(stack.getKey())) {
			return false;
		}
		Comparison a = comparison == Comparison.DEFAULT_COMPARISON ? EmiComparisonDefaults.get(getKey()) : comparison;
		Comparison b = stack.comparison == Comparison.DEFAULT_COMPARISON ? EmiComparisonDefaults.get(stack.getKey()) : stack.comparison;
		if (a == b) {
			return a.compare(this, stack);
		} else {
			return a.compare(this, stack) && b.compare(this, stack);
		}
	}

	public boolean isEqual(EmiStack stack, Comparison comparison) {
		return getKey().equals(stack.getKey()) && comparison.compare(this, stack);
	}

	public abstract List<Text> getTooltipText();

	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		if (!getRemainder().isEmpty()) {
			list.add(new RemainderTooltipComponent(this));
		}
		return list;
	}

	public abstract Text getName();

	public abstract int getSubtype();

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EmiStack stack) {
			return this.isEqual(stack);
		} else if (obj instanceof EmiIngredient stack) {
			return EmiIngredient.areEqual(this, stack);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode() ^ (getKey() == null ? 0 : getKey().hashCode());
	}

	@Override
	public String toString() {
		String s = "" + getKey();
		NBTTagCompound nbt = getNbt();
		if (nbt != null) {
			s += nbt;
		}
		return s + " x" + getAmount();
	}

	public static EmiStack of(Prototype proto) {
		return new ItemEmiStack(proto.toStack());
	}

	public static EmiStack of(ItemStack stack) {
		if (ItemStacks.isEmpty(stack)) {
			return EmiStack.EMPTY;
		}
		return new ItemEmiStack(stack);
	}

	public static EmiStack of(ItemStack stack, long amount) {
		if (ItemStacks.isEmpty(stack)) {
			return EmiStack.EMPTY;
		}
		return new ItemEmiStack(stack, amount);
	}

    public static EmiIngredient ofPotentialTag(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return EmiStack.EMPTY;
        }
        return fromPotentialTag(stack, stack.stackSize);
    }

    public static EmiIngredient ofPotentialTag(ItemStack stack, long amount) {
        if (stack == null || stack.getItem() == null) {
            return EmiStack.EMPTY;
        }
        return fromPotentialTag(stack, amount);
    }

    private static EmiIngredient fromPotentialTag(ItemStack stack, long amount) {
        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            List<ItemStack> stacks = new ArrayList<>();
            stack.getItem().getSubItems(stack.getItem(), CreativeTabs.tabMisc, stacks);
            return EmiIngredient.of(stacks.stream().map(EmiStack::of).collect(Collectors.toList()));
        } else {
            return new ItemEmiStack(stack, amount);
        }
    }

	public static EmiStack of(Item item) {
		if (item == null) {
			return EMPTY;
		}
		return of(new ItemStack(item), 1);
	}

	public static EmiStack of(Item item, long amount) {
		if (item == null) {
			return EMPTY;
		}
		return of(new ItemStack(item), amount);
	}

	public static EmiStack of(Item item, long amount, int meta) {
		return of(new ItemStack(item, 1, meta), amount);
	}

	public static EmiStack of(Item item, NBTTagCompound nbt) {
		return of(item, nbt, 1, 0);
	}

	public static EmiStack of(Item item, NBTTagCompound nbt, long amount) {
		return new ItemEmiStack(item, nbt, amount, 0);
	}

	public static EmiStack of(Item item, NBTTagCompound nbt, long amount, int meta) {
		return new ItemEmiStack(item, nbt, amount, meta);
	}

	public static EmiStack of(DyeItem i) {
		return of(i.toStack());
	}

	public static EmiStack of(Block block) {
		if (block == null) {
			return EMPTY;
		}
		return of(new ItemStack(block), 1);
	}

	public static EmiStack of(Block block, long amount) {
		if (block == null) {
			return EMPTY;
		}
		return of(new ItemStack(block), amount);
	}

	public static EmiStack of(FluidStack stack) {
		return of(stack.getFluid(), stack.tag, stack.amount);
	}

	public static EmiStack of(Fluid fluid) {
		return of(fluid, EmiPort.emptyExtraData());
	}

	public static EmiStack of(Fluid fluid, long amount) {
		return of(fluid, EmiPort.emptyExtraData(), amount);
	}

	public static EmiStack of(Fluid fluid, NBTTagCompound nbt) {
		return of(fluid, nbt, 0);
	}

	public static EmiStack of(Fluid fluid, NBTTagCompound nbt, long amount) {
		if (fluid == null) {
			return EmiStack.EMPTY;
		}
		return new FluidEmiStack(fluid, nbt, amount);
	}

	static abstract class Entry<T> {
	}
}
