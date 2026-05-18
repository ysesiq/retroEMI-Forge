package dev.emi.emi.network;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketSetSlot;
import com.rewindmc.retroemi.RetroEMI;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class CreateItemC2SPacket implements EmiPacket {
	private int mode;
	private ItemStack stack;

	public CreateItemC2SPacket() {
	}

	public CreateItemC2SPacket(int mode, ItemStack stack) {
		this.mode = mode;
		this.stack = stack;
	}

	public void read(PacketBuffer buf) {
		this.mode = buf.readByte();
		ItemStack stack = ItemStack.EMPTY;
		try {
			stack = buf.readItemStack();
		} catch (IOException ignored) {
		}
		this.stack = stack;
	}

	@Override
	public void write(PacketBuffer buf) {
		buf.writeByte(mode);
		buf.writeItemStack(stack);
	}

	@Override
	public void apply(EntityPlayer player) {
		if ((player.canUseCommand(2, "give") || player.capabilities.isCreativeMode) && player.openContainer != null) {
			if (stack.isEmpty()) {
				if (mode == 1 && !player.inventory.getItemStack().isEmpty()) {
					EmiLog.info(player.getName() + " deleted " + player.inventory.getItemStack());
					player.inventory.setItemStack(stack);
				}
			} else {
				EmiLog.info(player.getName() + " cheated in " + stack);
				if (mode == 0) {
					RetroEMI.offerOrDrop(player, stack);
				} else if (mode == 1) {
					player.inventory.setItemStack(stack);
				}
			}
		}
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.CREATE_ITEM;
	}
}
