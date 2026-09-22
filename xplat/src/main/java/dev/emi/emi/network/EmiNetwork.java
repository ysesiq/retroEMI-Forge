package dev.emi.emi.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dev.emi.emi.EmiPort;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class EmiNetwork {
	public static final ResourceLocation FILL_RECIPE = EmiPort.id("emi:fill_recipe");
	public static final ResourceLocation CREATE_ITEM = EmiPort.id("emi:create_item");
	public static final ResourceLocation COMMAND = EmiPort.id("emi:command");
	public static final ResourceLocation CHESS = EmiPort.id("emi:chess");
	public static final ResourceLocation PING = EmiPort.id("emi:ping");
	private static BiConsumer<EntityPlayerMP, EmiPacket> clientSender;
	private static Consumer<EmiPacket> serverSender;

	public static void initServer(BiConsumer<EntityPlayerMP, EmiPacket> sender) {
		clientSender = sender;
	}

	public static void initClient(Consumer<EmiPacket> sender) {
		serverSender = sender;
	}

	public static void sendToClient(EntityPlayerMP player, EmiPacket packet) {
		clientSender.accept(player, packet);
	}

	public static void sendToServer(EmiPacket packet) {
		serverSender.accept(packet);
	}
}
