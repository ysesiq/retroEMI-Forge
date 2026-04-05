package com.rewindmc.retroemi;

import dev.emi.emi.network.EmiPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PacketReader {

	public static final Map<String, Function<PacketBuffer, EmiPacket>> clientReaders = new HashMap<>();
	public static final Map<String, Function<PacketBuffer, EmiPacket>> serverReaders = new HashMap<>();

	public static void registerServerPacketReader(ResourceLocation id, Function<PacketBuffer, EmiPacket> reader) {
		serverReaders.put(id.toString(), reader);
	}

	public static void registerClientPacketReader(ResourceLocation id, Function<PacketBuffer, EmiPacket> reader) {
		clientReaders.put(id.toString(), reader);
	}
}
