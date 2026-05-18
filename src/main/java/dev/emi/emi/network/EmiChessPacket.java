package dev.emi.emi.network;

import java.util.UUID;

import dev.emi.emi.chess.EmiChess;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public abstract class EmiChessPacket implements EmiPacket {
	protected UUID uuid;
	protected byte type, start, end;

	public EmiChessPacket() {
	}

	public EmiChessPacket(UUID uuid, byte type, byte start, byte end) {
		this.uuid = uuid;
		this.type = type;
		this.start = start;
		this.end = end;
	}

	public void read(PacketBuffer buf) {
		this.uuid = buf.readUniqueId();
		this.type = buf.readByte();
		this.start = buf.readByte();
		this.end = buf.readByte();
	}

	@Override
	public void write(PacketBuffer buf) {
		buf.writeUniqueId(uuid);
		buf.writeByte(type);
		buf.writeByte(start);
		buf.writeByte(end);
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.CHESS;
	}

	public static class S2C extends EmiChessPacket {

		public S2C(UUID uuid, byte type, byte start, byte end) {
			super(uuid, type, start, end);
		}

		public S2C() {
		}

		@Override
		public void apply(EntityPlayer player) {
			EmiChess.receiveNetwork(uuid, type, start, end);
		}
	}

	public static class C2S extends EmiChessPacket {

		public C2S(UUID uuid, byte type, byte start, byte end) {
			super(uuid, type, start, end);
		}

		public C2S() {
		}

		@Override
		public void apply(EntityPlayer player) {
			EntityPlayer opponent = player.getEntityWorld().getPlayerEntityByUUID(uuid);
			if (opponent instanceof EntityPlayerMP spe) {
				EmiNetwork.sendToClient(spe, new S2C(player.getUniqueID(), type, start, end));
			}
		}
	}
}
