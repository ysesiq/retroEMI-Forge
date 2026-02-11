package shim.net.minecraft.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public class DyeableItem {

	public static ItemStack blendAndSetColor(ItemStack stack, List<DyeItem> colors) {
		int[] is = new int[3];
		int i = 0;
		int j = 0;
		Item item = stack.getItem();
		ItemStack itemStack = stack.copy();
		itemStack.stackSize = 1;
		if (itemStack.getTagCompound() != null && itemStack.getTagCompound().hasKey("display") && itemStack.getTagCompound().getCompoundTag("display").hasKey("color")) {
			int k = itemStack.getTagCompound().getCompoundTag("display").getInteger("color");
			float f = (float)(k >> 16 & 0xFF) / 255.0F;
			float g = (float)(k >> 8 & 0xFF) / 255.0F;
			float h = (float)(k & 0xFF) / 255.0F;
			i += (int)(Math.max(f, Math.max(g, h)) * 255.0F);
			is[0] += (int)(f * 255.0F);
			is[1] += (int)(g * 255.0F);
			is[2] += (int)(h * 255.0F);
			++j;
		}

		for(DyeItem dyeItem : colors) {
			float[] fs = dyeItem.color().getColorComponents();
			int l = (int)(fs[0] * 255.0F);
			int m = (int)(fs[1] * 255.0F);
			int n = (int)(fs[2] * 255.0F);
			i += Math.max(l, Math.max(m, n));
			is[0] += l;
			is[1] += m;
			is[2] += n;
			++j;
		}

		if (item == null) {
			return null;
		} else {
			int k = is[0] / j;
			int o = is[1] / j;
			int p = is[2] / j;
			float h = (float)i / (float)j;
			float q = (float)Math.max(k, Math.max(o, p));
			k = (int)((float)k * h / q);
			o = (int)((float)o * h / q);
			p = (int)((float)p * h / q);
			int var26 = (k << 8) + o;
			var26 = (var26 << 8) + p;
			if (itemStack.getTagCompound() == null) itemStack.setTagCompound(new NBTTagCompound());
			if (!itemStack.getTagCompound().hasKey("display")) itemStack.getTagCompound().setTag("display", new NBTTagCompound());
			itemStack.getTagCompound().getCompoundTag("display").setInteger("color", var26);
			return itemStack;
		}
	}
}
