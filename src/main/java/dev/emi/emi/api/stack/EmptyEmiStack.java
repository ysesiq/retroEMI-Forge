package dev.emi.emi.api.stack;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import dev.emi.emi.EmiPort;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import shim.net.minecraft.text.Text;
import net.minecraft.util.ResourceLocation;
import com.rewindmc.retroemi.ItemStacks;

@ApiStatus.Internal
public class EmptyEmiStack extends EmiStack {
	private static final ResourceLocation ID = EmiPort.id("emi", "empty");

	@Override
	public EmiStack getRemainder() {
		return EMPTY;
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return com.rewindmc.retroemi.shim.java.List.of(EMPTY);
	}

	@Override
	public EmiStack setRemainder(EmiStack stack) {
		throw new UnsupportedOperationException("Cannot mutate an empty stack");
	}

	@Override
	public EmiStack copy() {
		return EMPTY;
	}

	public EmiStack setAmount(long amount) {
		return this;
	}

	public EmiStack setChance(float chance) {
		return this;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public NBTTagCompound getNbt() {
		return null;
	}

	@Override
	public Object getKey() {
		return Blocks.air;
	}

	@Override
	public ItemStack getItemStack() {
		return ItemStacks.EMPTY;
	}

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public boolean isEqual(EmiStack stack) {
		return stack == EMPTY;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta, int flags) {
	}

	@Override
	public List<Text> getTooltipText() {
		return com.rewindmc.retroemi.shim.java.List.of();
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		return com.rewindmc.retroemi.shim.java.List.of();
	}

	@Override
	public Text getName() {
		return EmiPort.literal("");
	}

    @Override
    public int getSubtype() {
        return 0;
    }

    static class EmptyEntry {
	}
}
