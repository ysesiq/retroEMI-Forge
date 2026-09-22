package dev.emi.emi.mixin.jei.accessor;

import mezz.jei.gui.elements.GuiIconButton;
import mezz.jei.gui.elements.GuiIconToggleButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiIconToggleButton.class, remap = false)
public interface GuiIconToggleButtonAccessor {
	@Accessor("button") GuiIconButton getInternalButton();
}
