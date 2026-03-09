package dev.emi.emi.platform.forge;

import net.minecraftforge.fml.client.IModGuiFactory;
import dev.emi.emi.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Set;

public class EmiGuiFactory implements IModGuiFactory {
    @Override
    public void initialize(Minecraft client) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen screen) {
        return new ConfigScreen(screen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return shim.java.Set.of();
    }
}
