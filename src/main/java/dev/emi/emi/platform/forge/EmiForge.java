package dev.emi.emi.platform.forge;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
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
		EmiPacketHandler.init();
		if (event.getSide().isClient()) {
			EmiClientForge.clientInit();
			MinecraftForge.EVENT_BUS.register(new EmiClientForge());
		}
		EmiNetwork.initServer((player, packet) -> {
			EmiPacketHandler.CHANNEL.sendTo(EmiPacketHandler.wrap(packet), player);
		});
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {

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
			RetroEMI.tick();
		}
	}
}
