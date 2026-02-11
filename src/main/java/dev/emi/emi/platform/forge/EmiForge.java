package dev.emi.emi.platform.forge;

import com.rewindmc.retroemi.PacketReader;
import com.rewindmc.retroemi.RetroEMI;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import dev.emi.emi.EmiPort;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.data.EmiResourceManager;
import dev.emi.emi.data.EmiResourceReloadListener;
import dev.emi.emi.mixin.accessor.PlayerControllerMPAccessor;
import dev.emi.emi.nemi.NemiPlugin;
import dev.emi.emi.network.*;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayerMP;
import shim.net.minecraft.network.PacketByteBuf;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.common.MinecraftForge;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

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
        if (Loader.isModLoaded("NotEnoughItems")) {
            NemiPlugin.onLoad();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        EmiMain.init();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            Client.init();
            MinecraftForge.EVENT_BUS.register(new EmiClientForge());
        }
        EmiNetwork.initServer((player, packet) -> {
            player.playerNetServerHandler.sendPacket(toVanilla(packet));
        });
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);

        EmiTags.registerTagModels(Minecraft.getMinecraft().getResourceManager(), id -> {});
    }

    @Mod.EventHandler
    public void postInit(FMLInitializationEvent event) {
        EmiPort.registerReloadListeners((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager());
        PacketReader.registerServerPacketReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new);
        PacketReader.registerServerPacketReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new);
        PacketReader.registerServerPacketReader(EmiNetwork.CHESS, EmiChessPacket.C2S::new);
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(EmiResourceManager.INSTANCE);
    }

//    @SubscribeEvent
//    public void registerCommands(FMLServerStartingEvent event) {
//        event.registerServerCommand(new EmiCommands());
//    }

    @SubscribeEvent
    public void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP spe) {
            EmiNetwork.sendToClient(spe, new PingS2CPacket(spe.mcServer.isDedicatedServer() || (spe.mcServer instanceof IntegratedServer integratedServer && integratedServer.getPublic())));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            RetroEMI.tick();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            RetroEMI.tick();
        }
    }

    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (!event.isLocal) {
            EmiReloadManager.reload();
            EmiClient.onServer = true;
        }
    }

    @SubscribeEvent
    public void onClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        EmiLog.info("Disconnecting from server, EMI data cleared");
        EmiReloadManager.clear();
        EmiClient.onServer = false;
    }

    private static S3FPacketCustomPayload toVanilla(EmiPacket packet) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        PacketByteBuf buf = PacketByteBuf.out(dos);
        packet.write(buf);
        return new S3FPacketCustomPayload(RetroEMI.compactify(packet.getId()), baos.toByteArray());
    }

    public static final class Client {

        public static void init() {
            EmiClient.init();
            EmiData.init(EmiResourceReloadListener::reload);

            EmiNetwork.initClient(packet -> ((PlayerControllerMPAccessor) Minecraft.getMinecraft().playerController).getNetClientHandler().addToSendQueue(toVanilla(packet)));
            PacketReader.registerClientPacketReader(EmiNetwork.PING, PingS2CPacket::new);
            PacketReader.registerClientPacketReader(EmiNetwork.COMMAND, CommandS2CPacket::new);
            PacketReader.registerClientPacketReader(EmiNetwork.CHESS, EmiChessPacket.S2C::new);
        }
    }
}
