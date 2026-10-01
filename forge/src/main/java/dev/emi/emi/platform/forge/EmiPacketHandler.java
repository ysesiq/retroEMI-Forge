package dev.emi.emi.platform.forge;

import cpw.mods.fml.relauncher.SideOnly;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

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
			packet.apply(context.getServerHandler().playerEntity);
			return null;
		}
	}

	public static class CreateItemC2SPacketHandler implements IMessageHandler<CreateItemC2SPacket, IMessage> {
		@Override
		public IMessage onMessage(CreateItemC2SPacket packet, MessageContext context) {
				packet.apply(context.getServerHandler().playerEntity);
			return null;
		}
	}

	public static class EmiChessC2SPacketHandler implements IMessageHandler<EmiChessPacket.C2S, IMessage> {
		@Override
		public IMessage onMessage(EmiChessPacket.C2S packet, MessageContext context) {
			packet.apply(context.getServerHandler().playerEntity);
			return null;
		}
	}

	public static class PingS2CPacketHandler implements IMessageHandler<PingS2CPacket, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(PingS2CPacket packet, MessageContext context) {
			// Must be EntityPlayer, not EntityPlayerSP: on the server this method is stripped
			// by SideTransformer but the synthetic lambda method survives, so a client-only
			// captured type would make the class fail to load on the dedicated server.
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			Minecraft.getMinecraft().func_152344_a(() -> {
				packet.apply(player);
			});
			return null;
		}
	}

	public static class CommandS2CPacketHandler implements IMessageHandler<CommandS2CPacket, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(CommandS2CPacket packet, MessageContext context) {
			packet.apply(Minecraft.getMinecraft().thePlayer);
			return null;
		}
	}

	public static class EmiChessS2CPacketHandler implements IMessageHandler<EmiChessPacket.S2C, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(EmiChessPacket.S2C packet, MessageContext context) {
			packet.apply(Minecraft.getMinecraft().thePlayer);
			return null;
		}
	}
}
