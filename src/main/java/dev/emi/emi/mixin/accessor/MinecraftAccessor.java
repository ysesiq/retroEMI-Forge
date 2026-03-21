package dev.emi.emi.mixin.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("modelManager")
    ModelManager getModelManager();
}
