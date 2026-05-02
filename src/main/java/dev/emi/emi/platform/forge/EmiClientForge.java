package dev.emi.emi.platform.forge;

import com.rewindmc.retroemi.EmiResourceManager;
import com.rewindmc.retroemi.PacketReader;
import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import dev.emi.emi.data.EmiData;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import shim.net.minecraft.resource.ResourceReloader;

public class EmiClientForge {

	public static void clientInit() {
		EmiClient.init();
		EmiNetwork.initClient(packet -> Minecraft.getMinecraft().playerController.connection.sendPacket(EmiForge.toVanilla(packet)));
		PacketReader.registerClientPacketReader(EmiNetwork.PING, PingS2CPacket::new);
		PacketReader.registerClientPacketReader(EmiNetwork.COMMAND, CommandS2CPacket::new);
		PacketReader.registerClientPacketReader(EmiNetwork.CHESS, EmiChessPacket.S2C::new);
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(EmiResourceManager.instance);
	}

	public static void registerAdditionalModels() {
		Minecraft client = Minecraft.getMinecraft();
		EmiTags.registerTagModels(client.getResourceManager(), id -> {}, "inventory");
	}

	public static void registerResourceReloaders() {
		EmiData.init(ResourceReloader::reload);
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
		GuiScreen screen = event.getGui();
		if (!(screen instanceof GuiContainer)) {
			return;
		}
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			context.push();
			context.matrices().translate(-((GuiContainer) screen).getGuiLeft(), -((GuiContainer) screen).getGuiTop(), 0.0);
			EmiPort.setPositionTexShader();
			EmiScreenManager.drawBackground(context, event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
			context.pop();
		}
	}

	@SubscribeEvent
	public void postRenderScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
		EmiDrawContext context = EmiDrawContext.instance();
		GuiScreen screen = event.getGui();
		if (!(screen instanceof GuiContainer)) {
			return;
		}
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			context.push();
			EmiPort.setPositionTexShader();
			EmiScreenManager.render(context, event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
			EmiScreenManager.drawForeground(context, event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
			context.pop();
		}
	}

	@SubscribeEvent
	public void onMousePost(GuiScreenEvent.MouseInputEvent.Post event) {
		if (!(event.getGui() instanceof GuiContainerCreative)) {
			event.setCanceled(RetroEMI.handleMouseInput());
		}
	}

	@SubscribeEvent
	public void onKeyboardPost(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (!(event.getGui() instanceof GuiContainerCreative)) {
			event.setCanceled(RetroEMI.handleKeyboardInput());
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
		if (!event.isLocal()) {
			EmiReloadManager.reload();
			EmiClientForge.recipesReloaded();
			EmiClientForge.tagsReloaded();
//			EmiClient.onServer = true;
		}
	}

	@SubscribeEvent
	public void onClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		EmiLog.info("Disconnecting from server, EMI data cleared");
		EmiReloadManager.clear();
		EmiClient.onServer = false;
	}
}
