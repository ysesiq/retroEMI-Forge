package com.rewindmc.retroemi.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.emi.emi.EmiPort;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class EmiJappaResourcePack implements IResourcePack {
	private static final ResourceLocation BUTTONS = EmiPort.id("emi", "textures/gui/buttons.png");
	private static final String BUTTONS_MODERNITY = "/assets/emi/textures/gui/buttons_modernity.png";

	public EmiJappaResourcePack() {
	}

	public @Nullable InputStream getInputStream(@NonNull ResourceLocation id) {
		return BUTTONS.equals(id) ? EmiJappaResourcePack.class.getResourceAsStream(BUTTONS_MODERNITY) : null;
	}

	public boolean resourceExists(@NonNull ResourceLocation id) {
		return BUTTONS.equals(id) && EmiJappaResourcePack.class.getResource(BUTTONS_MODERNITY) != null;
	}

//	public @Nullable InputStream getInputStream(ResourceLocation id) {
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
	public IMetadataSection getPackMetadata(MetadataSerializer serializer, String section) {
		JsonObject json = new JsonParser().parse("{\"pack\":{\"description\":\"Modernity textures for Waystones-X, by DarkBum\",\"pack_format\":1}}").getAsJsonObject();
		return serializer.parseMetadataSection(section, json);	}

	public BufferedImage getPackImage() throws IOException {
		return ImageIO.read(EmiJappaResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("icon.png")).getPath()));
	}

	public String getPackName() {
		return "EMI Modernity Textures";
	}

//	private static String path(ResourceLocation id) {
//		return "assets/" + id.getNamespace() + "/" + id.getPath();
//	}
}
