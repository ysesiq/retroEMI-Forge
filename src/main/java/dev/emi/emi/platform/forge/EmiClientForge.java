package dev.emi.emi.platform.forge;

import com.rewindmc.retroemi.PacketReader;
import com.rewindmc.retroemi.RetroEMI;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import dev.emi.emi.EmiPort;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.EmiResourceReloadListener;
import dev.emi.emi.mixin.accessor.GuiContainerAccessor;
import dev.emi.emi.mixin.accessor.PlayerControllerMPAccessor;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.screen.EmiScreenBase;
import dev.emi.emi.screen.EmiScreenManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.event.GuiScreenEvent;

public class EmiClientForge {

	public static void clientInit() {
		EmiClient.init();
		EmiNetwork.initClient(packet -> ((PlayerControllerMPAccessor) Minecraft.getMinecraft().playerController).getNetClientHandler().addToSendQueue(EmiForge.toVanilla(packet)));
		PacketReader.registerClientPacketReader(EmiNetwork.PING, PingS2CPacket::new);
		PacketReader.registerClientPacketReader(EmiNetwork.COMMAND, CommandS2CPacket::new);
		PacketReader.registerClientPacketReader(EmiNetwork.CHESS, EmiChessPacket.S2C::new);
		RetroEMI.registerReloadListeners((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager());
		EmiClientForge.registerAdditionalModels();
		EmiClientForge.registerResourceReloaders();
	}

	public static void registerAdditionalModels() {
		Minecraft client = Minecraft.getMinecraft();
		EmiTags.registerTagModels(client.getResourceManager(), id -> {});
	}

	public static void registerResourceReloaders() {
		EmiData.init(EmiResourceReloadListener::reload);
	}

	public static void recipesReloaded() {
		EmiReloadManager.reloadRecipes();
	}

	public static void tagsReloaded() {
		EmiReloadManager.reloadTags();
	}

	@SubscribeEvent
	public void renderScreenForeground(GuiScreenEvent.DrawScreenEvent.Pre event) {
		EmiDrawContext context = EmiDrawContext.instance();
		GuiScreen screen = event.gui;
		if (!(screen instanceof GuiContainer)) {
			return;
		}
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			context.push();
			context.matrices().translate(-((GuiContainerAccessor) screen).getGuiLeft(), -((GuiContainerAccessor) screen).getGuiTop(), 0.0);
			EmiPort.setPositionTexShader();
			EmiScreenManager.drawBackground(context, event.mouseX, event.mouseY, event.renderPartialTicks);
			context.pop();
		}
	}

	@SubscribeEvent
	public void postRenderScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		EmiDrawContext context = EmiDrawContext.instance();
		GuiScreen screen = event.gui;
		if (!(screen instanceof GuiContainer)) {
			return;
		}
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			context.push();
			EmiPort.setPositionTexShader();
			EmiScreenManager.render(context, event.mouseX, event.mouseY, event.renderPartialTicks);
			EmiScreenManager.drawForeground(context, event.mouseX, event.mouseY, event.renderPartialTicks);
			context.pop();
		}
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			RetroEMI.tick();
		}
	}

	@SubscribeEvent
	public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
		if (!event.isLocal) {
			EmiReloadManager.reload();
			EmiClientForge.recipesReloaded();
			EmiClientForge.tagsReloaded();
			EmiClient.onServer = true;
		}
	}

	@SubscribeEvent
	public void onClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		EmiLog.info("Disconnecting from server, EMI data cleared");
		EmiReloadManager.clear();
		EmiClient.onServer = false;
	}
}
