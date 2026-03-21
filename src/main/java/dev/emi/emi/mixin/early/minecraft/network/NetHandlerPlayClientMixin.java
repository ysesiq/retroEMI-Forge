package dev.emi.emi.mixin.early.minecraft.network;

import com.rewindmc.retroemi.PacketReader;
import dev.emi.emi.network.EmiPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    public void handleCustomPayload(SPacketCustomPayload packetIn, CallbackInfo ci) {
        Function<PacketBuffer, EmiPacket> reader = PacketReader.clientReaders.get(packetIn.getChannelName());
        if (reader != null) {
            var epkt = reader.apply(new PacketBuffer(Unpooled.wrappedBuffer(packetIn.getBufferData())));
            epkt.apply(Minecraft.getMinecraft().player);
        }
    }
}
