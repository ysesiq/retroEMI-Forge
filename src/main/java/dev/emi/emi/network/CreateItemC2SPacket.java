package dev.emi.emi.network;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.ResourceLocation;
import com.rewindmc.retroemi.ItemStacks;
import com.rewindmc.retroemi.RetroEMI;

import java.io.IOException;

public class CreateItemC2SPacket implements EmiPacket {
	private final int mode;
	private final ItemStack stack;

	public CreateItemC2SPacket(int mode, ItemStack stack) {
		this.mode = mode;
		this.stack = stack;
	}

	public CreateItemC2SPacket(PacketBuffer buf) {
        ItemStack stack = ItemStack.EMPTY;
        try {
            stack = buf.readItemStack();
        } catch (IOException ignored) {
        }
        this.mode = buf.readByte();
        this.stack = stack;
	}

	@Override
	public void write(PacketBuffer buf) {
		buf.writeByte(mode);
		buf.writeItemStack(stack);
	}

	@Override
	public void apply(EntityPlayer player) {
		if (player instanceof EntityPlayerMP esp &&
				((esp.server.getPlayerList().getOppedPlayers().getEntry(esp.getGameProfile()).getPermissionLevel() >= 2) || player.capabilities.isCreativeMode) && //isPlayerOpped
				player.openContainer != null) {
			if (ItemStacks.isEmpty(stack)) {
				if (mode == 1) {
					player.inventory.setItemStack(null);
				}
			} else {
				EmiLog.info(player.getDisplayName() + " cheated in " + stack);
				if (mode == 0) {
					RetroEMI.offerOrDrop(player, stack);
				} else if (mode == 1) {
					player.inventory.setItemStack(stack);
					esp.connection.sendPacket(new SPacketSetSlot(-1, 0, stack));
				}
			}
		}
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.CREATE_ITEM;
	}
}
