package dev.emi.emi.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.network.PacketByteBuf;

public interface EmiPacket {

	void write(PacketByteBuf buf);

	void apply(EntityPlayer player);

	ResourceLocation getId();
}
