package dev.emi.emi.network;

import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class PingS2CPacket implements EmiPacket {

	public PingS2CPacket() {
	}

	public void read(PacketBuffer buf) {
	}

	@Override
	public void write(PacketBuffer buf) {
	}

	@Override
	public void apply(EntityPlayer player) {
		EmiClient.onServer = true;
		EmiReloadManager.reload();
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.PING;
	}
}
