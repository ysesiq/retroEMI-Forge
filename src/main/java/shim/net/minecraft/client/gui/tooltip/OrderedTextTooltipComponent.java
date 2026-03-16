package shim.net.minecraft.client.gui.tooltip;

import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shim.net.minecraft.text.OrderedText;

@SideOnly(Side.CLIENT)
public class OrderedTextTooltipComponent implements TooltipComponent {
    private final OrderedText text;

    public OrderedTextTooltipComponent(OrderedText text) {
        this.text = text;
    }

    @Override
    public int getWidth(FontRenderer textRenderer) {
        return textRenderer.getStringWidth(this.text.asString());
    }

    @Override
    public int getHeight() {
        return 10;
    }

    public void drawText(FontRenderer textRenderer, int x, int y) {
        textRenderer.drawString(this.text.asString(), x, y, -1, true);
    }

    public OrderedText getText() {
        return this.text;
    }
}
