package shim.net.minecraft.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.profiler.Profiler;

public abstract class SinglePreparationResourceReloader<T> implements ResourceReloader {

	@Override
	public void reload() {
		apply(prepare(Minecraft.getMinecraft().getResourceManager(), Minecraft.getMinecraft().mcProfiler), Minecraft.getMinecraft().getResourceManager(), Minecraft.getMinecraft().mcProfiler);
	}

	protected abstract T prepare(IResourceManager manager, Profiler profiler);

	protected abstract void apply(T t, IResourceManager manager, Profiler profiler);
}
