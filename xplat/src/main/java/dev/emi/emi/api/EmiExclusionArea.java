package dev.emi.emi.api;

import java.util.function.Consumer;

import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.GuiScreen;

public interface EmiExclusionArea<T extends GuiScreen> {

	void addExclusionArea(T screen, Consumer<Bounds> consumer);
}
