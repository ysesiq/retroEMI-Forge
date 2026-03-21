package com.rewindmc.retroemi;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dev.emi.emi.EmiPort;
import dev.emi.emi.mixin.accessor.FallbackResourceManagerAccessor;
import dev.emi.emi.mixin.accessor.SimpleReloadableResourceManagerAccessor;
import dev.emi.emi.platform.forge.EmiClientForge;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import org.jetbrains.annotations.NotNull;

public class EmiResourceManager implements ISelectiveResourceReloadListener {
    public static EmiResourceManager instance = new EmiResourceManager();

    @Override
    public void onResourceManagerReload(@NotNull IResourceManager resourceManager, @NotNull Predicate<IResourceType> predicate) {
        EmiClientForge.registerResourceReloaders();
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

        for (Map.Entry<String, ?> entry : ((SimpleReloadableResourceManagerAccessor) srm).getNamespaceResourceManagers().entrySet()) {
            String namespace = entry.getKey();
            Object frmObj = entry.getValue();

            if (!(frmObj instanceof FallbackResourceManager frm)) {
                continue;
            }

            String assetPrefix = "assets/" + namespace + "/";

            for (IResourcePack pack : ((FallbackResourceManagerAccessor) frm).getResourcePacks()) {
                if (!(pack instanceof FileResourcePack frp)) {
                    continue;
                }
                try (ZipFile zip = new ZipFile(frp.getResourcePackFile())) {
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
