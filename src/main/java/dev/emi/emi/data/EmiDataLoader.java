package dev.emi.emi.data;

import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.ResourceLocation;

public class EmiDataLoader<T>
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

//	@Override
	public T prepare(IResourceManager manager) {
		T t = baseSupplier.get();
		for (ResourceLocation id : EmiPort.findResources(manager, path, i -> i.endsWith(".json"))) {
            if (!id.getResourceDomain().equals("emi")) {
                continue;
            }
            try {
                for (Object resource : manager.getAllResources(id)) {
                    InputStreamReader reader = new InputStreamReader(EmiPort.getInputStream((IResource) resource));
                    JsonObject json = JsonHelper.deserialize(GSON, reader, JsonObject.class);
                    prepare.accept(t, json, id);

                }
            } catch (Exception e) {
                EmiLog.error("Error loading data for " + this.id + " in " + id, e);
            }
        }
		return t;
	}

    public void apply(T t) {
        apply.accept(t);
    }

	@Override
	public ResourceLocation getEmiId() {
		return id;
	}

    public void load(IResourceManager manager) {
        T data = prepare(manager);
        apply(data);
    }

    public interface DataConsumer<T> {
        void accept(T t, JsonObject json, ResourceLocation id);
    }

    public interface Supplier<T> {
        T get();
    }

    public interface Consumer<T> {
        void accept(T t);
    }
}
