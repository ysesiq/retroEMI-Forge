package dev.emi.emi.data;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringTranslate;

public class EmiResourceManager implements IResourceManagerReloadListener {
    public static final EmiResourceManager INSTANCE = new EmiResourceManager();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        Minecraft client = Minecraft.getMinecraft();
        ArrayList<String> arraylist = Lists.newArrayList("en_us");

        if (!"en_us".equals(client.gameSettings.language)) {
            arraylist.add(client.gameSettings.language.toLowerCase(java.util.Locale.ROOT));
        }

        this.loadLocaleDataFiles(resourceManager, arraylist);
        StringTranslate.replaceWith(getLanguageMap(I18n.i18nLocale));
    }

    public static Map<String, String> getLanguageMap(net.minecraft.client.resources.Locale locale) {
        Field languageListField;
        try {
            languageListField = Locale.class.getDeclaredField("field_135032_a");
            languageListField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) languageListField.get(locale);
            return map;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void loadLocaleDataFiles(IResourceManager resourceManager, List<String> languages) {
        for (String code : languages) {
            Collection<ResourceLocation> resourceLocations = EmiPort.findResources(resourceManager, "lang/" + code + ".json",
                fileName -> fileName.equals(code + ".json"));

            for (ResourceLocation location : resourceLocations) {
                try {
                    this.loadLocaleData(resourceManager.getAllResources(location));
                } catch (IOException exception) {
                    EmiLog.error("Error loading language file: " + location, exception);
                }
            }
        }
    }

    private void loadLocaleData(List<IResource> languages) throws IOException {
        for (IResource resource : languages) {
            this.loadLocaleData(resource.getInputStream());
        }
    }

    private void loadLocaleData(InputStream resourceStream) throws IOException {
        Gson gson = new Gson();

        try (InputStreamReader r = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
            JsonObject o = gson.fromJson(r, JsonObject.class);
            for (Map.Entry<String, JsonElement> map : o.entrySet()) {
//                LanguageRegistry.instance().addStringLocalization(map.getKey(), Minecraft.getMinecraft().gameSettings.language, map.getValue().getAsString());
                getLanguageMap(I18n.i18nLocale).put(map.getKey(), map.getValue().getAsString());
            }
        }
    }
}
