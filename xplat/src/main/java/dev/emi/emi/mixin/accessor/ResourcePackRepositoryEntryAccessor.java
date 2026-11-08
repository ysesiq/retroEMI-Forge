package dev.emi.emi.mixin.accessor;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.PackMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.awt.image.BufferedImage;

@Mixin(ResourcePackRepository.Entry.class)
public interface ResourcePackRepositoryEntryAccessor {

	@Accessor("reResourcePack")
	void setResourcePack(IResourcePack pack);

	@Accessor("rePackMetadataSection")
	void setMetadata(PackMetadataSection meta);

	@Accessor("texturePackIcon")
	void setIcon(BufferedImage icon);
}
