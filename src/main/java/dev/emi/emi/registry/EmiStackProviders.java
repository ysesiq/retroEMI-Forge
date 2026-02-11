package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.mixin.accessor.GuiContainerAccessor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import com.rewindmc.retroemi.ItemStacks;

public class EmiStackProviders {
	public static Map<Class<?>, List<EmiStackProvider<?>>> fromClass = Maps.newHashMap();
	public static List<EmiStackProvider<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static EmiStackInteraction getStackAt(GuiScreen screen, int x, int y, boolean notClick) {
		if (fromClass.containsKey(screen.getClass())) {
			for (EmiStackProvider provider : fromClass.get(screen.getClass())) {
				EmiStackInteraction stack = provider.getStackAt(screen, x, y);
				if (!stack.isEmpty() && (notClick || stack.isClickable())) {
					return stack;
				}
			}
		}
		for (EmiStackProvider handler : generic) {
			EmiStackInteraction stack = handler.getStackAt(screen, x, y);
			if (!stack.isEmpty() && (notClick || stack.isClickable())) {
				return stack;
			}
		}
		if (notClick && screen instanceof GuiContainer handled) {
			Slot s = ((GuiContainerAccessor) handled).getTheSlot();
			if (s != null) {
				ItemStack stack = s.getStack();
				if (!ItemStacks.isEmpty(stack)) {
					EmiStackInteraction stack1 = EmiStackProvidersClientOnly.getEmiStackInteraction(s, stack);
					if (stack1 != null) {
						return stack1;
					}
					return new EmiStackInteraction(EmiStack.of(stack));
				}
			}
		}
		return EmiStackInteraction.EMPTY;
	}
}
