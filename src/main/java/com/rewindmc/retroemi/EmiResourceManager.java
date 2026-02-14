package com.rewindmc.retroemi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.registry.LanguageRegistry;
import dev.emi.emi.EmiPort;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;

public class EmiResourceManager implements IResourceManagerReloadListener {
    public static EmiResourceManager instance = new EmiResourceManager();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        Minecraft client = Minecraft.getMinecraft();
        ArrayList<String> arraylist = Lists.newArrayList("en_us");

        if (!"en_us".equals(client.gameSettings.language)) {
            arraylist.add(client.gameSettings.language.toLowerCase(java.util.Locale.ROOT));
        }

        this.loadLocaleDataFiles(resourceManager, arraylist);
    }

    public synchronized void loadLocaleDataFiles(IResourceManager resourceManager, List<String> languages) {
        for (String code : languages) {
            ResourceLocation id = EmiPort.id("emi", "lang/" + code.toLowerCase(Locale.ROOT) + ".json");
            try {
                this.loadLocaleData(resourceManager.getAllResources(id));
            } catch (IOException ignore) {
            }
        }
    }

    private void loadLocaleData(List<IResource> resources) throws IOException {
        Gson gson = new Gson();

        for (IResource resource : resources) {
            try (InputStream stream = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject jo = gson.fromJson(reader, JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                    LanguageRegistry.instance().addStringLocalization(entry.getKey(), Minecraft.getMinecraft().gameSettings.language, entry.getValue().getAsString());
                }
            }
        }
    }

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

    public Map<ResourceLocation, IResource> findResources(IResourceManager manager, String startingPath, Predicate<ResourceLocation> allowedPathPredicate) {
        Map<ResourceLocation, IResource> result = new HashMap<>();
        if (!(manager instanceof SimpleReloadableResourceManager srm)) return result;

        boolean direct = startingPath.contains(".");
        int cut = startingPath.lastIndexOf('/');
        String folder = direct ? (cut >= 0 ? startingPath.substring(0, cut) : "") : startingPath;
        String fileName = direct ? (cut >= 0 ? startingPath.substring(cut + 1) : startingPath) : null;

        for (var entry : srm.domainResourceManagers.entrySet()) {
            String domain = ((Map.Entry<String, FallbackResourceManager>) entry).getKey();
            FallbackResourceManager frm = ((Map.Entry<String, FallbackResourceManager>) entry).getValue();
            String assetPrefix = "assets/" + domain + "/";
            String folderPrefix = folder.isEmpty() ? assetPrefix : assetPrefix + folder + "/";

            for (IResourcePack pack : (List<IResourcePack>) frm.resourcePacks) {

                if (pack instanceof FolderResourcePack frp) {
                    File root = new File(frp.resourcePackFile, folderPrefix);
                    if (!root.exists()) continue;

                    LinkedList<File> stack = new LinkedList<>();
                    stack.add(root);

                    String basePath = frp.resourcePackFile.getAbsolutePath().replace("\\", "/") + "/" + assetPrefix;

                    while (!stack.isEmpty()) {
                        File f = stack.removeFirst();
                        File[] list = f.listFiles();
                        if (list == null) continue;

                        for (File child : list) {
                            if (child.isDirectory()) {
                                stack.add(child);
                                continue;
                            }

                            if (direct && !child.getName().equals(fileName)) continue;

                            String rel = child.getAbsolutePath().replace("\\", "/").replace(basePath, "");
                            ResourceLocation id = EmiPort.id(domain, rel);
                            if (!allowedPathPredicate.test(id)) continue;

                            try { result.put(id, manager.getResource(id)); } catch (IOException ignored) {}
                        }
                    }
                }

                else if (pack instanceof FileResourcePack frp) {
                    try (ZipFile zip = new ZipFile(frp.resourcePackFile)) {
                        Enumeration<? extends ZipEntry> en = zip.entries();

                        while (en.hasMoreElements()) {
                            ZipEntry ze = en.nextElement();
                            if (ze.isDirectory()) continue;

                            String name = ze.getName();
                            if (!name.startsWith(assetPrefix)) continue;

                            String rel = name.substring(assetPrefix.length());
                            if (direct ? !rel.equals(startingPath) : !rel.startsWith(folder + "/")) continue;

                            ResourceLocation id = EmiPort.id(domain, rel);
                            if (!allowedPathPredicate.test(id)) continue;

                            try {
                                result.put(id, manager.getResource(id));
                            } catch (IOException ignored) {}
                        }
                    } catch (IOException ignored) {}
                }
            }
        }
        return result;
    }
}
