package dev.emi.emi.runtime;

import net.minecraft.client.Minecraft;

public class EmiProfiler {
	private static final Minecraft CLIENT = Minecraft.getMinecraft();

	public static void push(String name) {
		CLIENT.mcProfiler.startSection(name);
	}

	public static void pop() {
		CLIENT.mcProfiler.endSection();
	}

	public static void swap(String name) {
		CLIENT.mcProfiler.endStartSection(name);
	}
}
