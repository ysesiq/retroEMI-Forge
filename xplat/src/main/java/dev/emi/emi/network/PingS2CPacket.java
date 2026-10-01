package dev.emi.emi.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import dev.emi.emi.platform.EmiClient;
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
	@SideOnly(Side.CLIENT)
	public void apply(EntityPlayer player) {
		EmiClient.onServer = true;
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.PING;
	}
}
