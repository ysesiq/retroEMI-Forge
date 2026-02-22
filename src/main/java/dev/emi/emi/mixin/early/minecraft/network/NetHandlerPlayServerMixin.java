package dev.emi.emi.mixin.early.minecraft.network;

import com.rewindmc.retroemi.PacketReader;
import dev.emi.emi.network.EmiPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shim.net.minecraft.network.PacketByteBuf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.function.Function;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow public EntityPlayerMP playerEntity;

    @Inject(method = "processVanilla250Packet", at = @At("RETURN"))
    public void handleCustomPayload(C17PacketCustomPayload packetIn, CallbackInfo ci) {
        Function<PacketByteBuf, EmiPacket> reader = PacketReader.serverReaders.get(packetIn.func_149559_c());
        if (reader != null) {
            var epkt = reader.apply(PacketByteBuf.in(new DataInputStream(new ByteArrayInputStream(packetIn.func_149558_e()))));
            epkt.apply(playerEntity);
        }
    }
}
