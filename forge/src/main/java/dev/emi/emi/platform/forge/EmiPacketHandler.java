package dev.emi.emi.platform.forge;

import com.gtnewhorizon.gtnhlib.ClientProxy;
import cpw.mods.fml.common.FMLCommonHandler;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
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

        boolean isClient = FMLCommonHandler.instance().getSide().isClient();
        CHANNEL.registerMessage(isClient ? ClientProxy.getPingHandler() : new DummyClientHandler<>(), PingS2CPacket.class, i++, Side.CLIENT);
        CHANNEL.registerMessage(isClient ? ClientProxy.getCommandHandler() : new DummyClientHandler<>(), CommandS2CPacket.class, i++, Side.CLIENT);
        CHANNEL.registerMessage(isClient ? ClientProxy.getChessHandler() : new DummyClientHandler<>(), EmiChessPacket.S2C.class, i++, Side.CLIENT);
    }

	public static EmiPacket wrap(EmiPacket packet) {
		return packet;
	}

    private static class ClientProxy {
        static IMessageHandler<PingS2CPacket, IMessage> getPingHandler() { return new PingS2CPacketHandler(); }
        static IMessageHandler<CommandS2CPacket, IMessage> getCommandHandler() { return new CommandS2CPacketHandler(); }
        static IMessageHandler<EmiChessPacket.S2C, IMessage> getChessHandler() { return new EmiChessS2CPacketHandler(); }
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
		public IMessage onMessage(PingS2CPacket packet, MessageContext context) {
			EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
			Minecraft.getMinecraft().func_152344_a(() -> {
				packet.apply(player);
			});
			return null;
		}
	}

	public static class CommandS2CPacketHandler implements IMessageHandler<CommandS2CPacket, IMessage> {
		@Override
		public IMessage onMessage(CommandS2CPacket packet, MessageContext context) {
			packet.apply(Minecraft.getMinecraft().thePlayer);
			return null;
		}
	}

	public static class EmiChessS2CPacketHandler implements IMessageHandler<EmiChessPacket.S2C, IMessage> {
		@Override
		public IMessage onMessage(EmiChessPacket.S2C packet, MessageContext context) {
			packet.apply(Minecraft.getMinecraft().thePlayer);
			return null;
		}
	}

    public static class DummyClientHandler<T extends IMessage> implements IMessageHandler<T, IMessage> {
        @Override
        public IMessage onMessage(T message, MessageContext ctx) {
            return null;
        }
    }
}
