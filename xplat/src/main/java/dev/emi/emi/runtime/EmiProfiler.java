package dev.emi.emi.runtime;

import net.minecraft.client.Minecraft;

public class EmiProfiler {
	private static final Minecraft CLIENT = Minecraft.getMinecraft();

	public static void push(String name) {
		CLIENT.profiler.startSection(name);
	}

	public static void pop() {
		CLIENT.profiler.endSection();
	}

	public static void swap(String name) {
		CLIENT.profiler.endStartSection(name);
	}
}
