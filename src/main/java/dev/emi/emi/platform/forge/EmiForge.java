package dev.emi.emi.platform.forge;

import com.rewindmc.retroemi.PacketReader;
import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.common.MinecraftForge;

@Mod(
	modid = "emi",
	name = "EMI",
	guiFactory = "dev.emi.emi.platform.forge.EmiGuiFactory",
	dependencies =
		"required-after:mixinbooter@[4.2,);" +
		"after:jei;"
)
public class EmiForge {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		EmiMain.init();
		if (event.getSide().isClient()) {
			EmiClientForge.clientInit();
			MinecraftForge.EVENT_BUS.register(new EmiClientForge());
		}
		EmiNetwork.initServer((player, packet) -> {
			player.connection.sendPacket(toVanilla(packet));
		});
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (event.getSide().isServer()) {
			PacketReader.registerServerPacketReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new);
			PacketReader.registerServerPacketReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new);
			PacketReader.registerServerPacketReader(EmiNetwork.CHESS, EmiChessPacket.C2S::new);
		}
	}

	@Mod.EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new EmiCommands());
	}

	@SubscribeEvent
	public void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP spe) {
			EmiNetwork.sendToClient(spe, new PingS2CPacket(spe.server.isDedicatedServer() || (spe.server instanceof IntegratedServer integratedServer && integratedServer.getPublic())));
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			RetroEMI.tick();
		}
	}

	public static SPacketCustomPayload toVanilla(EmiPacket packet) {
		PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
		packet.write(buf);
		return new SPacketCustomPayload(packet.getId().toString(), buf);
	}
}
