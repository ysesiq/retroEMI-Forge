package shim.net.minecraft.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class SinglePreparationResourceReloader<T> implements ResourceReloader {

	@Override
	public void reload() {
		apply(prepare(Minecraft.getMinecraft().getResourceManager(), Minecraft.getMinecraft().profiler), Minecraft.getMinecraft().getResourceManager(), Minecraft.getMinecraft().profiler);
	}

	protected abstract T prepare(IResourceManager manager, Profiler profiler);

	protected abstract void apply(T t, IResourceManager manager, Profiler profiler);
}
