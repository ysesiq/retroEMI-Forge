package com.rewindmc.retroemi;

import dev.emi.emi.network.EmiPacket;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PacketReader {

    public static final Map<String, Function<PacketByteBuf, EmiPacket>> clientReaders = new HashMap<>();
    public static final Map<String, Function<PacketByteBuf, EmiPacket>> serverReaders = new HashMap<>();

    public static void registerServerPacketReader(ResourceLocation id, Function<PacketByteBuf, EmiPacket> reader) {
        serverReaders.put(RetroEMI.compactify(id), reader);
    }

    public static void registerClientPacketReader(ResourceLocation id, Function<PacketByteBuf, EmiPacket> reader) {
        clientReaders.put(RetroEMI.compactify(id), reader);
    }
}
