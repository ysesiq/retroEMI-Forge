package dev.emi.emi.mixin.accessor;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiContainer.class)
public interface GuiContainerAccessor {
	@Accessor("theSlot")
	Slot getTheSlot();

	@Accessor("guiLeft")
	int getGuiLeft();

	@Accessor("guiTop")
	int getGuiTop();

	@Accessor("xSize")
	int getXSize();

	@Accessor("ySize")
	int getYSize();
}
