package dev.emi.emi.mixin;

import com.rewindmc.retroemi.PacketReader;
import dev.emi.emi.network.EmiPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP player;

    @Inject(method = "processCustomPayload", at = @At("RETURN"))
    public void handleCustomPayload(CPacketCustomPayload packetIn, CallbackInfo ci) {
        Function<PacketBuffer, EmiPacket> reader = PacketReader.serverReaders.get(packetIn.getChannelName());
        if (reader != null) {
            PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(packetIn.getBufferData()));
            var epkt = reader.apply(buffer);
            epkt.apply(player);
        }
    }
}
