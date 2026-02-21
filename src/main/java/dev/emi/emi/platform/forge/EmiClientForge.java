package dev.emi.emi.platform.forge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenBase;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;

public class EmiClientForge {

//    @SubscribeEvent
//    public static void clientInit(FMLClientSetupEvent event) {
//        StackBatcher.EXTRA_RENDER_LAYERS.addAll(Arrays.stream(ForgeRenderTypes.values()).map(f -> f.get()).toList());
//        EmiClient.init();
//        EmiNetwork.initClient(packet -> EmiPacketHandler.CHANNEL.send(packet, MinecraftClient.getInstance().getNetworkHandler().getConnection()));
//        MinecraftForge.EVENT_BUS.addListener(EmiClientForge::recipesReloaded);
//        MinecraftForge.EVENT_BUS.addListener(EmiClientForge::tagsReloaded);
//        MinecraftForge.EVENT_BUS.addListener(EmiClientForge::renderScreenForeground);
//        MinecraftForge.EVENT_BUS.addListener(EmiClientForge::postRenderScreen);
//        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
//            () -> new ConfigScreenHandler.ConfigScreenFactory((client, last) -> new ConfigScreen(last)));
//    }
//
//    @SubscribeEvent
//    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
//        MinecraftClient client = MinecraftClient.getInstance();
//        EmiTags.registerTagModels(client.getResourceManager(), event::register);
//    }
//
//    @SubscribeEvent
//    public static void registerResourceReloaders(RegisterClientReloadListenersEvent event) {
//        EmiData.init(reloader -> event.registerReloadListener(reloader));
//    }
//
//    public static void recipesReloaded(RecipesUpdatedEvent event) {
//        EmiReloadManager.reloadRecipes();
//    }
//
//    public static void tagsReloaded(TagsUpdatedEvent event) {
//        EmiReloadManager.reloadTags();
//    }

    @SubscribeEvent
    public void renderScreenBackground(GuiScreenEvent.DrawScreenEvent.Pre event) {
        EmiDrawContext context = EmiDrawContext.instance();
        if (event.gui instanceof GuiContainer screen) {
            EmiScreenBase base = EmiScreenBase.of(screen);
            if (base != null) {
                EmiScreenManager.drawBackground(context, event.mouseX, event.mouseY, event.renderPartialTicks);
            }
        }
    }

    @SubscribeEvent
    public void renderScreenForeground(GuiScreenEvent.DrawScreenEvent.Post event) {
        EmiDrawContext context = EmiDrawContext.instance();
        if (event.gui instanceof GuiContainer screen) {
            EmiScreenBase base = EmiScreenBase.of(screen);
            if (base != null) {
                context.push();
                EmiPort.setPositionTexShader();
                EmiScreenManager.render(context, event.mouseX, event.mouseY, event.renderPartialTicks);
                EmiScreenManager.drawForeground(context, event.mouseX, event.mouseY, event.renderPartialTicks);
                context.pop();
            }
        }
    }
}
