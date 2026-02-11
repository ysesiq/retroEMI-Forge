package shim.net.minecraft.client.resource.language;

import java.util.Objects;

public class I18n {
    public static boolean hasTranslation(String key) {
        return !Objects.equals(net.minecraft.client.resources.I18n.format(key), key);
    }
}
