package dev.emi.emi.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import shim.net.minecraft.network.packet.CustomPayload;

public interface EmiPacket extends CustomPayload {

	void write(PacketBuffer buf);

	void read(PacketBuffer buf);

	void apply(EntityPlayer player);

	@Override
	default void fromBytes(ByteBuf byteBuf) {
		read(new PacketBuffer(byteBuf));
	}

	@Override
	default void toBytes(ByteBuf byteBuf) {
		write(new PacketBuffer(byteBuf));
	}
}
