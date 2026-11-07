package dev.emi.emi.runtime;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

public class EmiHistory {
	private static final List<GuiScreen> HISTORIES = Lists.newArrayList();
	private static final List<GuiScreen> FORWARD_HISTORIES = Lists.newArrayList();

	public static boolean isEmpty() {
		return HISTORIES.isEmpty();
	}

	public static boolean isForwardEmpty() {
		return FORWARD_HISTORIES.isEmpty();
	}

	public static void push(GuiScreen history) {
		HISTORIES.add(history);
		FORWARD_HISTORIES.clear();
	}

	public static void pop() {
		Minecraft client = Minecraft.getMinecraft();
		if (client.currentScreen instanceof GuiContainer) {
			clear();
			return;
		}
		int i = HISTORIES.size() - 1;
		GuiContainer screen = EmiApi.getHandledScreen();
		if (i >= 0) {
			GuiScreen popped = HISTORIES.remove(i);
			FORWARD_HISTORIES.add(client.currentScreen);
			client.displayGuiScreen(popped);
		} else if (screen != null) {
			client.displayGuiScreen(screen);
		}
	}

	public static void popUntil(Predicate<GuiScreen> predicate, GuiScreen otherwise) {
		Minecraft client = Minecraft.getMinecraft();
		while (!EmiHistory.isEmpty()) {
			EmiHistory.pop();
			if (predicate.test(client.currentScreen)) {
				return;
			}
		}
		client.displayGuiScreen(otherwise);
	}

	public static void forward() {
		Minecraft client = Minecraft.getMinecraft();
		int i = FORWARD_HISTORIES.size() - 1;
		if (i >= 0 && client.currentScreen != null) {
			GuiScreen popped = FORWARD_HISTORIES.remove(i);
			HISTORIES.add(client.currentScreen);
			client.displayGuiScreen(popped);
		}
	}

	public static void clear() {
		HISTORIES.clear();
		FORWARD_HISTORIES.clear();
	}
}
