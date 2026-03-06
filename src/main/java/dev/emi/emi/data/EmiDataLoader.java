package dev.emi.emi.data;

import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import shim.net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.util.JsonHelper;
import net.minecraft.profiler.Profiler;

public class EmiDataLoader<T> extends SinglePreparationResourceReloader<T>
		implements EmiResourceReloadListener {
	private static final Gson GSON = new Gson();
	private final ResourceLocation id;
	private final String path;
	private final Supplier<T> baseSupplier;
	private final DataConsumer<T> prepare;
	private final Consumer<T> apply;

	public EmiDataLoader(ResourceLocation id, String path, Supplier<T> baseSupplier,
			DataConsumer<T> prepare, Consumer<T> apply) {
		this.id = id;
		this.path = path;
		this.baseSupplier = baseSupplier;
		this.prepare = prepare;
		this.apply = apply;
	}

	@Override
	public T prepare(IResourceManager manager, Profiler profiler) {
		T t = baseSupplier.get();
		for (ResourceLocation id : EmiPort.findResources(manager, path, i -> i.endsWith(".json"))) {
			if (!id.getResourceDomain().equals("emi")) {
				continue;
			}
			try {
				for (IResource resource : (List<IResource>) manager.getAllResources(id)) {
					InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream(resource));
					JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
					prepare.accept(t, json, id);
				}
			} catch (Exception e) {
				EmiLog.error("Error loading data for " + this.id + " in " + id, e);
			}
		}
		return t;
	}

	@Override
	public void apply(T t, IResourceManager manager, Profiler profiler) {
		apply.accept(t);
	}

	@Override
	public ResourceLocation getEmiId() {
		return id;
	}


	public static interface DataConsumer<T> {
		void accept(T t, JsonObject json, ResourceLocation id);
	}
}
