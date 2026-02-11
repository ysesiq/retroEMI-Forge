package dev.emi.emi.mixin.early.minecraft.packet;

import com.rewindmc.retroemi.PacketReader;
import dev.emi.emi.network.EmiPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.PacketByteBuf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.function.Function;

@Mixin(NetHandlerPlayClient.class)
public class NetClientHandlerMixin {

    @Inject(method = "handleCustomPayload", at = @At("HEAD"))
    public void handleCustomPayload(S3FPacketCustomPayload packetIn, CallbackInfo ci) {
        Function<PacketByteBuf, EmiPacket> reader = PacketReader.clientReaders.get(packetIn.func_149169_c());
        if (reader != null) {
            var epkt = reader.apply(PacketByteBuf.in(new DataInputStream(new ByteArrayInputStream(packetIn.func_149168_d()))));
            epkt.apply(Minecraft.getMinecraft().thePlayer);
        }
    }
}
