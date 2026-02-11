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
            ResourceLocation id = new ResourceLocation("emi", "lang/" + code.toLowerCase(Locale.ROOT) + ".json");
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

        if (!(manager instanceof SimpleReloadableResourceManager srm)) {
            return result;
        }

        String folderOnly = startingPath;
        String directFileName = null;
        boolean isDirectFile = startingPath.contains(".");

        if (isDirectFile) {
            int idx = startingPath.lastIndexOf('/');
            if (idx >= 0) {
                folderOnly = startingPath.substring(0, idx);
                directFileName = startingPath.substring(idx + 1);
            } else {
                folderOnly = "";
                directFileName = startingPath;
            }
        }

        Map<String, FallbackResourceManager> domainManagers = srm.domainResourceManagers;

        for (Map.Entry<String, FallbackResourceManager> entry : domainManagers.entrySet()) {
            String domain = entry.getKey();
            FallbackResourceManager frm = entry.getValue();
            List<IResourcePack> packs = frm.resourcePacks;

            for (IResourcePack pack : packs) {

                if (pack instanceof FolderResourcePack) {
                    File base = ((FolderResourcePack) pack).resourcePackFile;
                    File dir = new File(base, "assets/" + domain + (folderOnly.isEmpty() ? "" : "/" + folderOnly));

                    if (dir.exists()) {
                        LinkedList<File> stack = new LinkedList<>();
                        stack.add(dir);

                        while (!stack.isEmpty()) {
                            File f = stack.removeFirst();
                            File[] children = f.listFiles();
                            if (children != null) {
                                for (File child : children) {
                                    if (child.isDirectory()) {
                                        stack.add(child);
                                    } else {
                                        String name = child.getName();
                                        boolean match = !isDirectFile || name.equals(directFileName);

                                        if (match) {
                                            String basePath = base.getAbsolutePath().replace("\\", "/");
                                            String fullPath = child.getAbsolutePath().replace("\\", "/")
                                                .replace(basePath + "/assets/" + domain + "/", "");

                                            ResourceLocation id = new ResourceLocation(domain, fullPath);
                                            if (allowedPathPredicate.test(id)) {
                                                try {
                                                    result.put(id, manager.getResource(id));
                                                } catch (IOException ignored) {
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else if (pack instanceof FileResourcePack) {
                    File zipFile = ((FileResourcePack) pack).resourcePackFile;

                    try (ZipFile zip = new ZipFile(zipFile)) {
                        Enumeration<? extends ZipEntry> entries = zip.entries();

                        while (entries.hasMoreElements()) {
                            ZipEntry ze = entries.nextElement();
                            if (ze.isDirectory()) continue;

                            String name = ze.getName();
                            if (!name.startsWith("assets/" + domain)) continue;

                            String relative = name.substring(("assets/" + domain + "/").length());

                            boolean match;
                            if (isDirectFile) {
                                match = relative.equals(startingPath);
                            } else {
                                match = relative.startsWith(folderOnly + "/");
                            }

                            if (match) {
                                ResourceLocation id = new ResourceLocation(domain, relative);
                                if (allowedPathPredicate.test(id)) {
                                    try {
                                        result.put(id, manager.getResource(id));
                                    } catch (IOException ignored) {
                                    }
                                }
                            }
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        }

        System.out.println("found resource:" + result);
        return result;
    }
}
