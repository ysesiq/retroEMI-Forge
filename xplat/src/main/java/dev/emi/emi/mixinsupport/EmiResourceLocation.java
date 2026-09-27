package dev.emi.emi.mixinsupport;

import net.minecraft.util.ResourceLocation;

public interface EmiResourceLocation {
	default int compareTo(ResourceLocation that) {
		return 0;
	}
}
