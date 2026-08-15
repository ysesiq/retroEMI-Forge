package com.rewindmc.retroemi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class EmiJappaResourcePack implements IResourcePack {

	public EmiJappaResourcePack() {
	}

	public @Nullable InputStream getInputStream(ResourceLocation id) {
		return EmiJappaResourcePack.class.getResourceAsStream("/assets/jappa/" + path(id));
	}

	public boolean resourceExists(ResourceLocation id) {
		return EmiJappaResourcePack.class.getResource("/assets/jappa/" + path(id)) != null;
	}

	public Set getResourceDomains() {
		return shim.java.Set.of("emi");
	}

	// not need
	public IMetadataSection getPackMetadata(MetadataSerializer serializer, String section) {
		JsonObject json = new JsonParser().parse("{\"pack\":{\"description\":\"Modernity textures for Waystones-X, by DarkBum\",\"pack_format\":1}}").getAsJsonObject();
		return serializer.parseMetadataSection(section, json);	}

	public BufferedImage getPackImage() throws IOException {
		return ImageIO.read(EmiJappaResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("icon.png")).getPath()));
	}

	public String getPackName() {
		return "EMI Modernity Textures";
	}

	private static String path(ResourceLocation id) {
		return "assets/" + id.getNamespace() + "/" + id.getPath();
	}
}
