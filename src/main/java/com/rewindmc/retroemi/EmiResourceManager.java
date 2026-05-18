package com.rewindmc.retroemi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dev.emi.emi.EmiPort;
import dev.emi.emi.platform.forge.EmiClientForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LegacyV2Adapter;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class EmiResourceManager implements ISelectiveResourceReloadListener {
	public static EmiResourceManager instance = new EmiResourceManager();

	@Override
	public void onResourceManagerReload(@NotNull IResourceManager resourceManager, @NotNull Predicate<IResourceType> predicate) {
		EmiClientForge.registerResourceReloaders();
		EmiClientForge.registerAdditionalModels();
		if (Minecraft.getMinecraft().world != null) {
			EmiClientForge.tagsReloaded();
			EmiClientForge.recipesReloaded();
		}
	}

	public Map<ResourceLocation, IResource> findResources(IResourceManager manager, String startingPath, Predicate<ResourceLocation> allowedPathPredicate) {
		Map<ResourceLocation, IResource> result = new HashMap<>();
		if (!(manager instanceof SimpleReloadableResourceManager srm)) {
			return result;
		}
		for (Map.Entry<String, ?> entry : srm.domainResourceManagers.entrySet()) {
			String namespace = entry.getKey();
			if (!(entry.getValue() instanceof FallbackResourceManager frm)) {
				continue;
			}
			String assetPrefix = String.format("assets/%s/", namespace);
			for (IResourcePack pack : frm.resourcePacks) {
				if (pack instanceof LegacyV2Adapter adapter) {
					pack = adapter.getUnadaptedPack();
				}
				if (pack instanceof FileResourcePack frp) {
					try (ZipFile zip = new ZipFile(frp.getResourcePackFile())) {
						Stream<String> relativePaths = zip.stream()
							.filter(ze -> !ze.isDirectory())
							.map(ZipEntry::getName)
							.filter(p -> p.startsWith(assetPrefix))
							.map(p -> p.substring(assetPrefix.length()));
						processRelativePaths(relativePaths, namespace, startingPath, allowedPathPredicate, manager, result);
					} catch (IOException ignored) {
					}
				} else if (pack instanceof FMLFolderResourcePack ffrp) { // For dev environment
					Path assets = ffrp.getResourcePackFile().toPath().resolve(assetPrefix);
					if (!Files.isDirectory(assets)) {
						continue;
					}
					try (Stream<Path> walk = Files.walk(assets)) {
						Stream<String> relativePaths = walk
							.filter(Files::isRegularFile)
							.map(assets::relativize)
							.map(Path::toString)
							.map(p -> p.replace(File.separatorChar, '/'));
						processRelativePaths(relativePaths, namespace, startingPath, allowedPathPredicate, manager, result);
					} catch (IOException ignored) {
					}
				}
			}
		}
		return result;
	}

	private void processRelativePaths(Stream<String> relativePaths, String namespace, String startingPath, Predicate<ResourceLocation> allowedPathPredicate, IResourceManager manager, Map<ResourceLocation, IResource> result) {
		relativePaths.filter(
			rel -> startingPath.contains(".") ? rel.equals(startingPath) : rel.startsWith(startingPath.isEmpty() ? "" : startingPath + "/"))
			.map(rel -> EmiPort.id(namespace, rel))
			.filter(allowedPathPredicate)
			.forEach(id -> {
				try {
					result.put(id, manager.getResource(id));
				} catch (IOException ignored) {
				}
			}
		);
	}
}
