package com.rewindmc.retroemi.client;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class EmiJappaResourcePack implements IResourcePack {

	public EmiJappaResourcePack() {
	}

	public @Nullable InputStream getInputStream(@NonNull ResourceLocation id) {
		return EmiJappaResourcePack.class.getResourceAsStream(modernityPath(id));
	}

	public boolean resourceExists(@NonNull ResourceLocation id) {
		return EmiJappaResourcePack.class.getResource(modernityPath(id)) != null;
	}

	private static String modernityPath(ResourceLocation id) {
		String path = id.getResourcePath();
		int i = path.lastIndexOf('.');
		if (i > path.lastIndexOf('/')) {
			path = path.substring(0, i) + "_modernity" + path.substring(i);
		} else {
			path = path + "_modernity";
		}
		return "/assets/" + id.getResourceDomain() + "/" + path;
	}

	public Set getResourceDomains() {
		return shim.java.Set.of("emi");
	}

	// not need
	public IMetadataSection getPackMetadata(IMetadataSerializer serializer, String section) {
		return null;
	}

	public BufferedImage getPackImage() throws IOException {
		return ImageIO.read(EmiJappaResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("icon.png")).getResourcePath()));
	}

	public String getPackName() {
		return "EMI Modernity Textures";
	}
}
