package dev.emi.emi.mixin.accessor;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LegacyV2Adapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LegacyV2Adapter.class)
public interface LegacyV2AdapterAccessor {
	@Accessor("pack")
	IResourcePack getUnadaptedPack();
}
