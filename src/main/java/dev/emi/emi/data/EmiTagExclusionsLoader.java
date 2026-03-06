package dev.emi.emi.data;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import shim.net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.util.JsonHelper;
import net.minecraft.profiler.Profiler;

public class EmiTagExclusionsLoader extends SinglePreparationResourceReloader<TagExclusions>
		implements EmiResourceReloadListener {
	private static final Gson GSON = new Gson();
	private static final ResourceLocation ID = EmiPort.id("emi:tag_exclusions");

	@Override
	public TagExclusions prepare(IResourceManager manager, Profiler profiler) {
		TagExclusions exclusions = new TagExclusions();
		for (ResourceLocation id : EmiPort.findResources(manager, "tag/exclusions", i -> i.endsWith(".json"))) {
			if (!id.getResourceDomain().equals("emi")) {
				continue;
			}
			try {
				for (IResource resource : (List<IResource>) manager.getAllResources(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
					try {
						if (JsonHelper.getBoolean(json, "replace", false)) {
							exclusions.clear();
						}
						for (String key : json.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList())) {
							ResourceLocation type = EmiPort.id(key);
							if (JsonHelper.hasArray(json, key)) {
								JsonArray arr = JsonHelper.getArray(json, key);
								for (JsonElement el : arr) {
									ResourceLocation eid = EmiPort.id(el.getAsString());
									if (key.equals("exclusions")) {
										exclusions.add(eid);
										if (eid.getResourceDomain().equals("c")) {
											exclusions.add(EmiPort.id("forge", eid.getResourcePath()));
										}
									} else {
										exclusions.add(type, eid);
										if (eid.getResourceDomain().equals("c")) {
											exclusions.add(type, EmiPort.id("forge", eid.getResourcePath()));
										}
									}
								}
							}
						}
					} catch (Exception e) {
						EmiLog.error("Error loading tag exclusions", e);
					}
				}
			} catch (Exception e) {
				EmiLog.error("Error loading tag exclusions", e);
			}
		}
		return exclusions;
	}

	@Override
	public void apply(TagExclusions exclusions, IResourceManager manager, Profiler profiler) {
		EmiTags.exclusions = exclusions;
	}

	@Override
	public ResourceLocation getEmiId() {
		return ID;
	}
}
