package dev.emi.emi.stack.serializer;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ItemEmiStackSerializer implements EmiStackSerializer<ItemEmiStack> {

	@Override
	public String getType() {
		return "item";
	}

	@Override
	public EmiStack create(ResourceLocation id, NBTTagCompound nbt, long amount, int subtype) {
		ItemStack stack = new ItemStack(EmiPort.getItemRegistry().getObject(id.toString()), 1, subtype);
		stack.setTagCompound(nbt);
		return EmiStack.of(stack, amount);
	}
}
