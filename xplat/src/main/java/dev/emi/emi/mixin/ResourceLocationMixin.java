package dev.emi.emi.mixin;

import dev.emi.emi.mixinsupport.EmiResourceLocation;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin implements EmiResourceLocation {
	@Mutable @Final @Shadow private final String resourceDomain;
	@Mutable @Final @Shadow private final String resourcePath;

	public ResourceLocationMixin(String resourceDomain, String resourcePath) {
		this.resourceDomain = resourceDomain;
		this.resourcePath = resourcePath;
	}

	@Override
	public int compareTo(ResourceLocation that) {
		int i = this.resourcePath.compareTo(that.getResourcePath());
		if (i != 0) return i;
		return this.resourceDomain.compareTo(that.getResourceDomain());
	}
}
