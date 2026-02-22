package dev.emi.emi.nemi;

import codechicken.nei.LayoutManager;
import codechicken.nei.LayoutStyleMinecraft;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.mixin.accessor.GuiContainerAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;
import java.util.List;

public class NemiPlugin implements EmiPlugin {
    private static final Minecraft client = Minecraft.getMinecraft();

    public static void onLoad() {
        try {
            Class<?> apiClass = Class.forName("codechicken.nei.api.API");
            Method registerMethod = apiClass.getMethod("registerNEIGuiHandler",
                Class.forName("codechicken.nei.api.INEIGuiHandler"));
            Object handler = new NemiScreenHandler();
            registerMethod.invoke(null, handler);
        } catch (Exception e) {
            EmiLog.error("Failed to register NEI GUI handler via reflection", e);
        }
    }

    @Override
    public void register(EmiRegistry registry) {
        if (EmiAgnos.isModLoaded("NotEnoughItems")) {
            registry.addGenericExclusionArea((screen, consumer) -> {
                final LayoutStyleMinecraft layout = (LayoutStyleMinecraft) LayoutManager.getLayoutStyle();
                if (layout != null && !(client.currentScreen instanceof RecipeScreen)) {
                    final int rows = (int) Math.ceil((double) layout.buttonCount / layout.numButtons);
                    final int diff = rows * 19 + 2;
                    consumer.accept(new Bounds(0, 0, layout.numButtons * 19, diff));
                }
            });

            registry.addGenericStackProvider((screen, x, y) -> {
                if (client.currentScreen instanceof GuiContainer guiContainer) {
                    ItemStack stack = null;
                    List<Slot> inventorySlots = guiContainer.inventorySlots.inventorySlots;
                    if (inventorySlots != null) {
                        for (Slot slot : inventorySlots) {
                            if (isMouseOverSlot(guiContainer, slot, x, y)) {
                                stack = slot.getStack();
                                break;
                            }
                        }
                    }
                    if (stack != null && stack.stackSize > 0) {
                        EmiStack emiStack = EmiStack.of(stack.getItem(), stack.getItemDamage());
                        return new EmiStackInteraction(emiStack);
                    }
                }
                return EmiStackInteraction.EMPTY;
            });
        }
    }

    private static boolean isMouseOverSlot(GuiContainer gui, Slot slot, int mouseX, int mouseY) {
        if (gui == null || slot == null) {
            return false;
        }

        return mouseX >= slot.xDisplayPosition + ((GuiContainerAccessor) gui).getGuiLeft() && mouseX < slot.xDisplayPosition + ((GuiContainerAccessor) gui).getGuiLeft() + 16 &&
               mouseY >= slot.yDisplayPosition + ((GuiContainerAccessor) gui).getGuiTop() && mouseY < slot.yDisplayPosition + ((GuiContainerAccessor) gui).getGuiTop() + 16;
    }
}
