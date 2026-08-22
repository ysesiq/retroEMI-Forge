package dev.emi.emi.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.jemi.IJeiSubtypeRegistry;
import mezz.jei.runtime.SubtypeRegistry;

@Mixin(value = SubtypeRegistry.class, remap = false)
public abstract class SubtypeRegistryMixin implements IJeiSubtypeRegistry {
}
