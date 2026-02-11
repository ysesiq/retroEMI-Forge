package dev.emi.emi.data;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.LanguageRegistry;
import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringTranslate;

public class EmiResourceManager implements IResourceManagerReloadListener {
    public static final EmiResourceManager INSTANCE = new EmiResourceManager();
    private static final Map<ResourceLocation, URL> files = new HashMap<>();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        Minecraft client = Minecraft.getMinecraft();
        ArrayList<String> arraylist = Lists.newArrayList("en_us");

        if (!"en_us".equals(client.gameSettings.language)) {
            arraylist.add(client.gameSettings.language.toLowerCase(java.util.Locale.ROOT));
        }

        this.loadLocaleDataFiles(resourceManager, arraylist);
//        StringTranslate.replaceWith(getLanguageMap(I18n.i18nLocale));

        File src = Loader.instance().activeModContainer().getSource();
        if (src.isFile()) {
            try (ZipFile zf = new ZipFile(src)) {
                for (ZipEntry en : Collections.list(zf.entries())) {
                    if (!en.getName().endsWith("/")) {
                        for (String namespace : (Set<String>) resourceManager.getResourceDomains()) {
                            files.put(new ResourceLocation(namespace, en.getName().substring(11)), EmiResourceManager.class.getResource("/" + en.getName()));
                        }
                    }
                }
            } catch (Throwable e) {
                EmiLog.error("Error scanning resources", e);
            }
        } else {
            throw new IllegalStateException("Mod file is a directory?");
        }
    }

//    public Map<ResourceLocation, EmiResource> findResources(String startingPath, Predicate<ResourceLocation> pathFilter) {
//        var map = new HashMap<ResourceLocation, EmiResource>();
//
//        files.entrySet().stream()
//            .filter(en -> en.getKey().getResourcePath().startsWith(startingPath+"/"))
//            .filter(en -> pathFilter.test(en.getKey()))
//            .forEach(en -> map.put(en.getKey(), new EmiResource(en.getValue()::openStream)));
//
//        return map;
//    }

//    public static Map<String, String> getLanguageMap(net.minecraft.client.resources.Locale locale) {
//        Field languageListField;
//        try {
//            languageListField = Locale.class.getDeclaredField("field_135032_a");
//            languageListField.setAccessible(true);
//
//            @SuppressWarnings("unchecked")
//            Map<String, String> map = (Map<String, String>) languageListField.get(locale);
//            return map;
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public synchronized void loadLocaleDataFiles(IResourceManager resourceManager, List<String> languages) {
        for (String code : languages) {
            Collection<ResourceLocation> resourceLocations = EmiPort.findResources(resourceManager, "lang/" + code + ".json",
                fileName -> fileName.equals(code + ".json"));

            for (ResourceLocation location : resourceLocations) {
                try {
                    this.loadLocaleData(resourceManager.getAllResources(location));
                } catch (IOException ignore) {
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
                LanguageRegistry.instance().addStringLocalization(map.getKey(), Minecraft.getMinecraft().gameSettings.language, map.getValue().getAsString());
//                getLanguageMap(I18n.i18nLocale).put(map.getKey(), map.getValue().getAsString());
            }
        }
    }
}
