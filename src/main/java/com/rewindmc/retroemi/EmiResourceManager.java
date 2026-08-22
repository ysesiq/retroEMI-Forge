package com.rewindmc.retroemi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dev.emi.emi.EmiPort;
import dev.emi.emi.mixin.accessor.AbstractResourcePackAccessor;
import dev.emi.emi.mixin.accessor.FallbackResourceManagerAccessor;
import dev.emi.emi.mixin.accessor.SimpleReloadableResourceManagerAccessor;
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
import cpw.mods.fml.client.FMLFolderResourcePack;
import org.jetbrains.annotations.NotNull;

public class EmiResourceManager implements IResourceManagerReloadListener {
	public static EmiResourceManager instance = new EmiResourceManager();
	private static final Map<IResourcePack, List<String>> PACK_PATHS = new WeakHashMap<>();

	@Override
	public void onResourceManagerReload(@NotNull IResourceManager resourceManager) {
		EmiClientForge.registerResourceReloaders();
		EmiClientForge.registerAdditionalModels();
		if (Minecraft.getMinecraft().theWorld != null) {
			EmiClientForge.tagsReloaded();
			EmiClientForge.recipesReloaded();
		}
	}

	public Map<ResourceLocation, IResource> findResources(IResourceManager manager, String startingPath, Predicate<ResourceLocation> allowedPathPredicate) {
		if (!(manager instanceof SimpleReloadableResourceManager srm)) {
			return shim.java.Map.of();
		}
		Map<ResourceLocation, IResource> result = new HashMap<>();
		for (Map.Entry<String, ?> entry : ((SimpleReloadableResourceManagerAccessor) srm).getDomainResourceManagers().entrySet()) {
			String namespace = entry.getKey();
			if (!(entry.getValue() instanceof FallbackResourceManager frm)) {
				continue;
			}
			String assetPrefix = "assets/" + namespace + "/";
			for (IResourcePack pack : ((FallbackResourceManagerAccessor) frm).getResourcePacks()) {
//				if (pack instanceof LegacyV2AdapterAccessor adapter) {
//					pack = adapter.getUnadaptedPack();
//				}
				Stream<String> relativePaths = getPackPaths(pack).stream()
					.filter(p -> p.startsWith(assetPrefix))
					.map(p -> p.substring(assetPrefix.length()));
				processRelativePaths(relativePaths, namespace, startingPath, allowedPathPredicate, manager, result);
			}
		}
		return result;
	}

	private void processRelativePaths(Stream<String> relativePaths, String namespace, String startingPath, Predicate<ResourceLocation> allowedPathPredicate, IResourceManager manager, Map<ResourceLocation, IResource> result) {
		String directoryPrefix = startingPath.isEmpty() ? "" : startingPath + "/";
		relativePaths
			.filter(rel -> startingPath.isEmpty() || rel.equals(startingPath) || rel.startsWith(directoryPrefix))
			.map(rel -> EmiPort.id(namespace, rel))
			.filter(allowedPathPredicate)
			.forEach(id -> {
				try {
					result.put(id, manager.getResource(id));
				} catch (IOException ignored) {
				}
			});
	}

	private static List<String> getPackPaths(IResourcePack pack) {
		synchronized (PACK_PATHS) {
			return PACK_PATHS.computeIfAbsent(pack, p -> {
				if (p instanceof FileResourcePack frp) {
					try (ZipFile zip = new ZipFile(((AbstractResourcePackAccessor) frp).getResourcePackFile())) {
						return zip.stream()
							.filter(ze -> !ze.isDirectory())
							.map(ZipEntry::getName)
							.filter(name -> name.startsWith("assets/"))
							.collect(Collectors.toList());
					} catch (IOException ignored) {
					}
				} else if (p instanceof FMLFolderResourcePack ffrp) { // For dev environment
					Path assets = ((AbstractResourcePackAccessor) ffrp).getResourcePackFile().toPath().resolve("assets");
					if (Files.isDirectory(assets)) {
						try (Stream<Path> walk = Files.walk(assets)) {
							return walk
								.filter(Files::isRegularFile)
								.map(assets::relativize)
								.map(Path::toString)
								.map(s -> s.replace(File.separatorChar, '/'))
								.map(s -> "assets/" + s)
								.collect(Collectors.toList());
						} catch (IOException ignored) {
						}
					}
				}
				return shim.java.List.of();
			});
		}
	}
}
