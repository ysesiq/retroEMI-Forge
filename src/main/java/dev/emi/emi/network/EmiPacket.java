package dev.emi.emi.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public interface EmiPacket {

	void write(PacketBuffer buf);

	void apply(EntityPlayer player);

	ResourceLocation getId();
}
