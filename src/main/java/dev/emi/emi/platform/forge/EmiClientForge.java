package dev.emi.emi.platform.forge;

import com.rewindmc.retroemi.EmiResourceManager;
import com.rewindmc.retroemi.RetroEMI;
import com.rewindmc.retroemi.RetroEMICommonUtils;
import dev.emi.emi.EmiPort;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.mixin.accessor.GuiContainerAccessor;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.nemi.NemiRecipe;
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
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import shim.net.minecraft.resource.ResourceReloader;

public class EmiClientForge {

	public static void clientInit() {
//		StackBatcher.EXTRA_RENDER_LAYERS.addAll(Arrays.stream(ForgeRenderTypes.values()).map(f -> f.get()).toList());
		EmiClient.init();
		EmiNetwork.initClient(packet -> EmiPacketHandler.CHANNEL.sendToServer(EmiPacketHandler.wrap(packet)));
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
		GuiScreen screen = event.gui;
		if (!(screen instanceof GuiContainer)) {
			return;
		}
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			context.push();
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
			EmiScreenManager.drawForeground(context, event.mouseX, event.mouseY, event.renderPartialTicks);
			context.pop();
		}
	}

//	@SubscribeEvent
//	public void onMousePost(GuiScreenEvent.MouseInputEvent.Pre event) {
//		if (!(event.getGui() instanceof GuiContainerCreative) && !RetroEMI.hasFocusedTextReflectField(event.getGui())) {
//			event.setCanceled(RetroEMI.handleMouseInput());
//		}
//	}
//
//	@SubscribeEvent
//	public void onKeyboardPost(GuiScreenEvent.KeyboardInputEvent.Pre event) {
//		if (!(event.getGui() instanceof GuiContainerCreative) && !RetroEMI.hasFocusedTextReflectField(event.getGui())) {
//			event.setCanceled(RetroEMI.handleKeyboardInput());
//		}
//	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			RetroEMICommonUtils.tick();
			NemiRecipe.tickHandlers();
		}
	}

//	@SubscribeEvent
//	public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
//		if (!event.isLocal()) {
//			EmiReloadManager.reload();
//		}
//	}

	@SubscribeEvent
	public void onClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		EmiLog.info("Disconnecting from server, EMI data cleared");
		EmiReloadManager.clear();
		EmiClient.onServer = false;
	}
}
