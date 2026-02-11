package dev.emi.emi.network;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.ResourceLocation;
import com.rewindmc.retroemi.ItemStacks;
import com.rewindmc.retroemi.RetroEMI;
import shim.net.minecraft.network.PacketByteBuf;

public class CreateItemC2SPacket implements EmiPacket {
	private final int mode;
	private final ItemStack stack;

	public CreateItemC2SPacket(int mode, ItemStack stack) {
		this.mode = mode;
		this.stack = stack;
	}

	public CreateItemC2SPacket(PacketByteBuf buf) {
		this(buf.readByte(), buf.readItemStack());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeByte(mode);
		buf.writeItemStack(stack);
	}

	@Override
	public void apply(EntityPlayer player) {
		if (player instanceof EntityPlayerMP esp &&
				((esp.mcServer.getConfigurationManager().func_152596_g(esp.getGameProfile())) || player.capabilities.isCreativeMode) && //isPlayerOpped
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
					esp.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, 0, stack));
				}
			}
		}
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.CREATE_ITEM;
	}
}
