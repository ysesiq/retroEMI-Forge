package dev.emi.emi.platform.forge;

import com.rewindmc.retroemi.RetroEMICommonUtils;
import cpw.mods.fml.common.FMLCommonHandler;
import dev.emi.emi.nemi.NemiPlugin;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import net.minecraft.entity.player.EntityPlayerMP;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(
	modid = "emi",
	name = "EMI",
	guiFactory = "dev.emi.emi.platform.forge.EmiGuiFactory",
	dependencies = """
			required-after:gtnhlib@[0.6.0,);\
			before:unimixins@[0.1,);\
			"""
)
public class EmiForge {

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		EmiAgnosForge.asmDataTable = event.getAsmData();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		EmiMain.init();
		EmiPacketHandler.init();
		if (event.getSide().isClient()) {
			EmiClientForge.clientInit();
			EmiClientForge client = new EmiClientForge();
			MinecraftForge.EVENT_BUS.register(client);
			FMLCommonHandler.instance().bus().register(client);
		}
		EmiNetwork.initServer((player, packet) -> {
			EmiPacketHandler.CHANNEL.sendTo(EmiPacketHandler.wrap(packet), player);
		});
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (EmiAgnos.isModLoaded("NotEnoughItems")) {
			NemiPlugin.onLoad();
		}
	}

	@Mod.EventHandler
	public void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(new EmiCommands());
	}

	@SubscribeEvent
	public void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP spe) {
			EmiNetwork.sendToClient(spe, new PingS2CPacket());
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			RetroEMICommonUtils.tick();
		}
	}
}
