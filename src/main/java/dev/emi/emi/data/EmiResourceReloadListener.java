package dev.emi.emi.data;

import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.resource.ResourceReloader;

public interface EmiResourceReloadListener extends ResourceReloader {

	ResourceLocation getEmiId();
}
