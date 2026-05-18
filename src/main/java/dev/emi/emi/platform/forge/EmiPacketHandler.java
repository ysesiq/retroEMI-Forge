package dev.emi.emi.platform.forge;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class EmiPacketHandler {
	public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("emi");

	public static void init() {
		int i = 0;
		CHANNEL.registerMessage(new FillRecipeC2SPacketHandler(), FillRecipeC2SPacket.class, i++, Side.SERVER);
		CHANNEL.registerMessage(new CreateItemC2SPacketHandler(), CreateItemC2SPacket.class, i++, Side.SERVER);
		CHANNEL.registerMessage(new EmiChessC2SPacketHandler(), EmiChessPacket.C2S.class, i++, Side.SERVER);
		CHANNEL.registerMessage(new PingS2CPacketHandler(), PingS2CPacket.class, i++, Side.CLIENT);
		CHANNEL.registerMessage(new CommandS2CPacketHandler(), CommandS2CPacket.class, i++, Side.CLIENT);
		CHANNEL.registerMessage(new EmiChessS2CPacketHandler(), EmiChessPacket.S2C.class, i++, Side.CLIENT);
	}

	public static EmiPacket wrap(EmiPacket packet) {
		return packet;
	}

	public static class FillRecipeC2SPacketHandler implements IMessageHandler<FillRecipeC2SPacket, IMessage> {
		@Override
		public IMessage onMessage(FillRecipeC2SPacket packet, MessageContext context) {
			packet.apply(context.getServerHandler().player);
			return null;
		}
	}

	public static class CreateItemC2SPacketHandler implements IMessageHandler<CreateItemC2SPacket, IMessage> {
		@Override
		public IMessage onMessage(CreateItemC2SPacket packet, MessageContext context) {
				packet.apply(context.getServerHandler().player);
			return null;
		}
	}

	public static class EmiChessC2SPacketHandler implements IMessageHandler<EmiChessPacket.C2S, IMessage> {
		@Override
		public IMessage onMessage(EmiChessPacket.C2S packet, MessageContext context) {
			packet.apply(context.getServerHandler().player);
			return null;
		}
	}

	public static class PingS2CPacketHandler implements IMessageHandler<PingS2CPacket, IMessage> {
		@Override
		public IMessage onMessage(PingS2CPacket packet, MessageContext context) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			Minecraft.getMinecraft().addScheduledTask(() -> {
				packet.apply(player);
			});
			return null;
		}
	}

	public static class CommandS2CPacketHandler implements IMessageHandler<CommandS2CPacket, IMessage> {
		@Override
		public IMessage onMessage(CommandS2CPacket packet, MessageContext context) {
			packet.apply(Minecraft.getMinecraft().player);
			return null;
		}
	}

	public static class EmiChessS2CPacketHandler implements IMessageHandler<EmiChessPacket.S2C, IMessage> {
		@Override
		public IMessage onMessage(EmiChessPacket.S2C packet, MessageContext context) {
			packet.apply(Minecraft.getMinecraft().player);
			return null;
		}
	}
}
