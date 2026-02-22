package dev.emi.emi.platform.forge;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import com.rewindmc.retroemi.PacketReader;
import com.rewindmc.retroemi.RetroEMI;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.emi.emi.mixin.accessor.PlayerControllerMPAccessor;
import dev.emi.emi.nemi.NemiPlugin;
import dev.emi.emi.network.*;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import net.minecraft.entity.player.EntityPlayerMP;
import shim.net.minecraft.network.PacketByteBuf;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.common.MinecraftForge;

@Mod(
	modid = "emi",
	name = "EMI",
	dependencies = """
			required-after:gtnhlib@[0.6.0,);\
			before:unimixins@[0.1,);\
			""",
	guiFactory = "dev.emi.emi.compat.EmiGuiFactory"
)
public class EmiForge {

	@Mod.EventHandler
	public void preInit(FMLInitializationEvent event) {
		if (EmiAgnos.isModLoaded("NotEnoughItems")) {
			NemiPlugin.onLoad();
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		EmiMain.init();
		if (FMLCommonHandler.instance().getSide().isClient()) {
			EmiClientForge.clientInit();
			MinecraftForge.EVENT_BUS.register(new EmiClientForge());
		}
		EmiNetwork.initServer((player, packet) -> {
			player.playerNetServerHandler.sendPacket(toVanilla(packet));
		});
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
    }

	@Mod.EventHandler
	public void postInit(FMLInitializationEvent event) {
		if (FMLCommonHandler.instance().getSide().isServer()) {
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
			EmiNetwork.sendToClient(spe, new PingS2CPacket(spe.mcServer.isDedicatedServer() || (spe.mcServer instanceof IntegratedServer integratedServer && integratedServer.getPublic())));
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			RetroEMI.tick();
		}
	}

	public static S3FPacketCustomPayload toVanilla(EmiPacket packet) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		PacketByteBuf buf = PacketByteBuf.out(dos);
		packet.write(buf);
		return new S3FPacketCustomPayload(packet.getId().toString(), baos.toByteArray());
	}
}
