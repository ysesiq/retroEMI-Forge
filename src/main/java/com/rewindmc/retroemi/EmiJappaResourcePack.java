package com.rewindmc.retroemi;

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
		return EmiJappaResourcePack.class.getResourceAsStream("/assets/emi/textures/gui/buttons_modernity.png");
	}

	public boolean resourceExists(@NonNull ResourceLocation id) {
		return EmiJappaResourcePack.class.getResource("/assets/emi/textures/gui/buttons_modernity.png") != null;
	}

//	public InputStream getInputStream(ResourceLocation id) {
//		return EmiJappaResourcePack.class.getResourceAsStream("/assets/jappa/" + path(id));
//	}
//
//	public boolean resourceExists(ResourceLocation id) {
//		return EmiJappaResourcePack.class.getResource("/assets/jappa/" + path(id)) != null;
//	}

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

//	private static String path(ResourceLocation id) {
//		return "assets/" + id.getResourceDomain() + "/" + id.getResourcePath();
//	}
}
