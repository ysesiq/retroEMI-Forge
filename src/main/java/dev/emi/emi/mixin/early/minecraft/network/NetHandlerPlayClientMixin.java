package dev.emi.emi.mixin.early.minecraft.network;

import com.rewindmc.retroemi.PacketReader;
import dev.emi.emi.network.EmiPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shim.net.minecraft.network.PacketByteBuf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.function.Function;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    public void handleCustomPayload(SPacketCustomPayload packetIn, CallbackInfo ci) {
        Function<PacketByteBuf, EmiPacket> reader = PacketReader.clientReaders.get(packetIn.getChannelName());
        if (reader != null) {
            var epkt = reader.apply(PacketByteBuf.in(new DataInputStream(new ByteArrayInputStream(packetIn.getBufferData().readByteArray()))));
            epkt.apply(Minecraft.getMinecraft().player);
        }
    }
}
