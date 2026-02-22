package shim.net.minecraft.text;

import net.minecraft.event.ClickEvent;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Style {

    public static final Style EMPTY = new Style("", false, null);

    final String formats;
    final ClickEvent clickEvent;
    final Boolean underline;

    public Style(String formats, Boolean underline, ClickEvent clickEvent) {
        this.formats = formats;
        this.clickEvent = clickEvent;
        this.underline = underline;
    }

    public Style withUnderline(boolean underline) {
        return Objects.equals(this.underline, underline) ? this : with(new Style(this.formats + EnumChatFormatting.UNDERLINE, underline, this.clickEvent), this.underline, underline);
    }

    public Style withColor(int color) {
        return Objects.equals(this.formats, String.valueOf(color)) ? this : with(new Style(formats + "§x" + (Integer.toHexString(color|0xFF000000).substring(2).replace("", "§")) + "x", underline, this.clickEvent), this.formats, String.valueOf(color));
    }

    public Style withFormatting(EnumChatFormatting formatting) {
        return Objects.equals(this.formats, formatting) ? this : with(new Style(this.formats, underline, this.clickEvent), this.formats, formatting);
    }

    public Style withClickEvent(ClickEvent clickEvent) {
        return Objects.equals(this.clickEvent, clickEvent) ? this : with(new Style(this.formats, this.underline, clickEvent), this.clickEvent, clickEvent);
    }

    private static <T> Style with(Style newStyle, @Nullable T oldAttribute, @Nullable T newAttribute) {
        return oldAttribute != null && newAttribute == null && newStyle.equals(EMPTY) ? EMPTY : newStyle;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    @Override
    public String toString() {
        return formats;
    }

}
