package com.rewindmc.retroemi;

import com.google.common.collect.Lists;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.data.EmiResourceReloadListener;
import dev.emi.emi.data.IndexStackData;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class EmiRemoveFromIndex implements EmiResourceReloadListener, IResourceManagerReloadListener {

	public static List<EmiIngredient> removed = Lists.newArrayList();
	public static List<IndexStackData.Added> added = Lists.newArrayList();
	public static List<IndexStackData.Filter> filter = Lists.newArrayList();
	public static IndexStackData entries;
	private static final ResourceLocation ID = EmiPort.id("emi", "removed_stacks");

	@Override
	public void onResourceManagerReload(IResourceManager var1) {
		entries = new IndexStackData(false, added, removed, filter);
	}

	@Override
	public ResourceLocation getEmiId() {
		return ID;
	}
}
