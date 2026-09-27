package dev.emi.emi.network;

import com.rewindmc.retroemi.RetroEMICommonUtils;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import com.rewindmc.retroemi.RetroEMI;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.item.ItemStacks;

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
		ItemStack stack = ItemStacks.EMPTY;
		try {
			stack = buf.readItemStackFromBuffer();
		} catch (IOException ignored) {
		}
		this.stack = stack;
	}

	@Override
	public void write(PacketBuffer buf) {
		buf.writeByte(mode);
		try {
			buf.writeItemStackToBuffer(stack);
		} catch (IOException ignored) {
		}
	}

	@Override
	public void apply(EntityPlayer player) {
		if ((player.canCommandSenderUseCommand(2, "give") || player.capabilities.isCreativeMode) && player.openContainer != null) {
			if (ItemStacks.isEmpty(stack)) {
				if (mode == 1 && !ItemStacks.isEmpty(player.inventory.getItemStack())) {
					EmiLog.info(player.getCommandSenderName() + " deleted " + player.inventory.getItemStack());
					player.inventory.setItemStack(stack);
				}
			} else {
				EmiLog.info(player.getCommandSenderName() + " cheated in " + stack);
				if (mode == 0) {
					RetroEMICommonUtils.offerOrDrop(player, stack);
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
