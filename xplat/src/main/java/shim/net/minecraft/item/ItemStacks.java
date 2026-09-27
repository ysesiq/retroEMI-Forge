package shim.net.minecraft.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStacks {

	public static final ItemStack EMPTY = null;

	public static boolean isEmpty(ItemStack stack) {
		return stack == null || stack.stackSize == 0 || stack.getItem() == null ? true : stack.getItem().delegate == null;
	}

	public static boolean isEmpty(Item item) {
		return isEmpty(new ItemStack(item));
	}

}
