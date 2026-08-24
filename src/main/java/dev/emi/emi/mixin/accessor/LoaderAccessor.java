package dev.emi.emi.mixin.accessor;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.discovery.ModDiscoverer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Loader.class, remap = false)
public interface LoaderAccessor {
	@Accessor("discoverer")
	ModDiscoverer getDiscoverer();
}
