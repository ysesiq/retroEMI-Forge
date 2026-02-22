package com.rewindmc.retroemi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.registry.LanguageRegistry;
import dev.emi.emi.EmiPort;
import dev.emi.emi.platform.forge.EmiClientForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

public class EmiResourceManager implements IResourceManagerReloadListener {
    public static EmiResourceManager instance = new EmiResourceManager();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        Minecraft client = Minecraft.getMinecraft();
        for (ResourceLocation id : EmiPort.findResources(resourceManager, "lang", s -> s.endsWith(".json"))) {
            if (!id.getResourceDomain().equals("emi")) {
                continue;
            }
            try {
                String code = id.getResourcePath().replace("lang/", "").replace(".json", "");
                if (code.equals(client.gameSettings.language.toLowerCase(Locale.ROOT))) {
                    this.loadLocaleData(resourceManager.getAllResources(id), client.gameSettings.language);
                }
            } catch (IOException ignore) {
            }
        }
    }

    private void loadLocaleData(List<IResource> resources, String code) throws IOException {
        Gson gson = new Gson();
        for (IResource resource : resources) {
            try (InputStream stream = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject jo = gson.fromJson(reader, JsonObject.class);
                for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                    LanguageRegistry.instance().addStringLocalization(entry.getKey(), code, entry.getValue().getAsString());
                }
            }
        }
    }

    public Map<ResourceLocation, IResource> findResources(IResourceManager manager, String startingPath, Predicate<ResourceLocation> allowedPathPredicate) {
        Map<ResourceLocation, IResource> result = new HashMap<>();
        if (!(manager instanceof SimpleReloadableResourceManager srm)) {
            return result;
        }

        boolean isDirect = startingPath.contains(".");
        int cut = startingPath.lastIndexOf('/');
        String folder = startingPath;

        if (isDirect) {
            folder = (cut < 0) ? "" : startingPath.substring(0, cut);
        }

        for (Object entry : srm.domainResourceManagers.entrySet()) {
            String namespace = ((Map.Entry<String, FallbackResourceManager>) entry).getKey();
            FallbackResourceManager frm = ((Map.Entry<String, FallbackResourceManager>) entry).getValue();
            String assetPrefix = "assets/" + namespace + "/";

            for (IResourcePack pack : (List<IResourcePack>) frm.resourcePacks) {
                if (!(pack instanceof FileResourcePack frp)) {
                    continue;
                }
                try (ZipFile zip = new ZipFile(frp.resourcePackFile)) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    String folderPrefix = folder.isEmpty() ? "" : folder + "/";
                    while (entries.hasMoreElements()) {
                        ZipEntry ze = entries.nextElement();
                        if (ze.isDirectory()) {
                            continue;
                        }

                        String fullPath = ze.getName();
                        if (!fullPath.startsWith(assetPrefix)) {
                            continue;
                        }

                        String rel = fullPath.substring(assetPrefix.length());

                        boolean pathMatches = isDirect ? rel.equals(startingPath) : rel.startsWith(folderPrefix);

                        if (!pathMatches) {
                            continue;
                        }

                        ResourceLocation id = EmiPort.id(namespace, rel);
                        if (!allowedPathPredicate.test(id)) {
                            continue;
                        }

                        try {
                            result.put(id, manager.getResource(id));
                        } catch (IOException ignored) {
                        }
                    }

                } catch (IOException ignored) {
                }
            }
        }
        return result;
    }
}
