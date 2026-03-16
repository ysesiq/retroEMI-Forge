package dev.emi.emi.mixin.early.minecraft.client;

import dev.emi.emi.mixinsupport.inject_interface.EmiResourceLocation;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ResourceLocation.class)
public class ResourceLocationMixin implements EmiResourceLocation {
    @Mutable @Final @Shadow protected final String namespace;
    @Mutable @Final @Shadow protected final String path;

    public ResourceLocationMixin(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    @Override
    public int compareTo(ResourceLocation that) {
        int i = this.path.compareTo(that.getPath());
        if (i != 0) return i;
        return this.namespace.compareTo(that.getNamespace());
    }
}
