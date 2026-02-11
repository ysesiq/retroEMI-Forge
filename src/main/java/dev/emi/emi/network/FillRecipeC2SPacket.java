package dev.emi.emi.network;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import com.rewindmc.retroemi.ItemStacks;
import com.rewindmc.retroemi.RetroEMI;
import shim.net.minecraft.network.PacketByteBuf;

public class FillRecipeC2SPacket implements EmiPacket {
	private final int syncId;
	private final int action;
	private final List<Integer> slots, crafting;
	private final int output;
	private final List<ItemStack> stacks;

	public FillRecipeC2SPacket(Container handler, int action, List<Slot> slots, List<Slot> crafting, @Nullable Slot output, List<ItemStack> stacks) {
		this.syncId = handler.windowId;
		this.action = action;
		this.slots = slots.stream().map(s -> s == null ? -1 : s.slotNumber).collect(Collectors.toList());
		this.crafting = crafting.stream().map(s -> s == null ? -1 : s.slotNumber).collect(Collectors.toList());
		this.output = output == null ? -1 : output.slotNumber;
		this.stacks = stacks;
	}

	public FillRecipeC2SPacket(PacketByteBuf buf) {
		syncId = buf.readInt();
		action = buf.readByte();
		slots = parseCompressedSlots(buf);
		crafting = Lists.newArrayList();
		int craftingSize = buf.readVarInt();
		for (int i = 0; i < craftingSize; i++) {
			int s = buf.readVarInt();
			crafting.add(s);
		}
		if (buf.readBoolean()) {
			output = buf.readVarInt();
		} else {
			output = -1;
		}
		int size = buf.readVarInt();
		stacks = Lists.newArrayList();
		for (int i = 0; i < size; i++) {
			stacks.add(buf.readItemStack());
		}
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeInt(syncId);
		buf.writeByte(action);
		writeCompressedSlots(slots, buf);
		buf.writeVarInt(crafting.size());
		for (Integer s : crafting) {
			buf.writeVarInt(s);
		}
		if (output != -1) {
			buf.writeBoolean(true);
			buf.writeVarInt(output);
		} else {
			buf.writeBoolean(false);
		}
		buf.writeVarInt(stacks.size());
		for (ItemStack stack : stacks) {
			buf.writeItemStack(stack);
		}
	}

	@Override
	public void apply(EntityPlayer player) {
		if (slots == null || crafting == null) {
			EmiLog.error("Client requested fill but passed input and crafting slot information was invalid, aborting");
			return;
		}
		Container handler = player.openContainer;
		if (handler == null || handler.windowId != syncId) {
			EmiLog.warn("Client requested fill but screen handler has changed, aborting");
			return;
		}
		List<Slot> slots = Lists.newArrayList();
		List<Slot> crafting = Lists.newArrayList();
		Slot output = null;
		for (int i : this.slots) {
			if (i < 0 || i >= handler.inventorySlots.size()) {
				EmiLog.error("Client requested fill but passed input slots don't exist, aborting");
				return;
			}
			slots.add(handler.getSlot(i));
		}
		for (int i : this.crafting) {
			if (i >= 0 && i < handler.inventorySlots.size()) {
				crafting.add(handler.getSlot(i));
			} else {
				crafting.add(null);
			}
		}
		if (this.output != -1) {
			if (this.output >= 0 && this.output < handler.inventorySlots.size()) {
				output = handler.getSlot(this.output);
			}
		}
		if (crafting.size() >= stacks.size()) {
			List<ItemStack> rubble = Lists.newArrayList();
			for (int i = 0; i < crafting.size(); i++) {
				Slot s = crafting.get(i);
				if (s != null && s.canTakeStack(player) && !ItemStacks.isEmpty(s.getStack())) {
					rubble.add(s.getStack().copy());
					s.putStack(ItemStacks.EMPTY);
				}
			}
			try {
				for (int i = 0; i < stacks.size(); i++) {
					ItemStack stack = stacks.get(i);
					if (ItemStacks.isEmpty(stack)) {
						continue;
					}
					int gotten = grabMatching(player, slots, rubble, crafting, stack);
					if (gotten != stack.stackSize) {
						if (gotten > 0) {
							stack.stackSize = (gotten);
							RetroEMI.offerOrDrop(player, stack);
						}
						return;
					} else {
						Slot s = crafting.get(i);
						if (s != null && s.isItemValid(stack) && stack.stackSize <= s.getSlotStackLimit()) {
							s.putStack(stack);
						} else {
							RetroEMI.offerOrDrop(player, stack);
						}
					}
				}
				if (output != null) {
					if (action == 1) {
						handler.slotClick(output.slotNumber, 0, 0, player);
					} else if (action == 2) {
						handler.slotClick(output.slotNumber, 0, 1, player);
					}
				}
			} finally {
				for (ItemStack stack : rubble) {
					RetroEMI.offerOrDrop(player, stack);
				}
			}
		}
	}

	private static List<Integer> parseCompressedSlots(PacketByteBuf buf) {
		List<Integer> list = Lists.newArrayList();
		int amount = buf.readVarInt();
		for (int i = 0; i < amount; i++) {
			int low = buf.readVarInt();
			int high = buf.readVarInt();
			if (low < 0) {
				return null;
			}
			for (int j = low; j <= high; j++) {
				list.add(j);
			}
		}
		return list;
	}

	private static void writeCompressedSlots(List<Integer> list, PacketByteBuf buf) {
		List<Consumer<PacketByteBuf>> postWrite = Lists.newArrayList();
		int groups = 0;
		int i = 0;
		while (i < list.size()) {
			groups++;
			int start = i;
			int startValue = list.get(start);
			while (i < list.size() && i - start == list.get(i) - startValue) {
				i++;
			}
			int end = i - 1;
			postWrite.add(b -> {
				b.writeVarInt(startValue);
				b.writeVarInt(list.get(end));
			});
		}
		buf.writeVarInt(groups);
		for (Consumer<PacketByteBuf> consumer : postWrite) {
			consumer.accept(buf);
		}
	}

	private static int grabMatching(EntityPlayer player, List<Slot> slots, List<ItemStack> rubble, List<Slot> crafting, ItemStack stack) {
		int amount = stack.stackSize;
		int grabbed = 0;
		for (int i = 0; i < rubble.size(); i++) {
			if (grabbed >= amount) {
				return grabbed;
			}
			ItemStack r = rubble.get(i);
			if (RetroEMI.canCombine(stack, r)) {
				int wanted = amount - grabbed;
				if (r.stackSize <= wanted) {
					grabbed += r.stackSize;
					rubble.remove(i);
					i--;
				} else {
					grabbed = amount;
					r.stackSize = (r.stackSize - wanted);
				}
			}
		}
		for (Slot s : slots) {
			if (grabbed >= amount) {
				return grabbed;
			}
			if (crafting.contains(s) || !s.canTakeStack(player)) {
				continue;
			}
			ItemStack st = s.getStack();
			if (RetroEMI.canCombine(stack, st)) {
				int wanted = amount - grabbed;
				if (st.stackSize <= wanted) {
					grabbed += st.stackSize;
					s.putStack(ItemStacks.EMPTY);
				} else {
					grabbed = amount;
					st.stackSize = (st.stackSize - wanted);
				}
			}
		}
		return grabbed;
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.FILL_RECIPE;
	}
}
