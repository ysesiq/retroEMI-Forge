package dev.emi.emi.mixin.accessor;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.PackMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.awt.image.BufferedImage;
import java.io.File;

@Mixin(ResourcePackRepository.Entry.class)
public interface ResourcePackRepositoryEntryAccessor {
	@Invoker("<init>")
	static ResourcePackRepository.Entry invokeNew(ResourcePackRepository repository, File file) {
		return null;
	}

	@Accessor("reResourcePack")
	void setResourcePack(IResourcePack pack);

	@Accessor("rePackMetadataSection")
	void setMetadata(PackMetadataSection meta);

	@Accessor("texturePackIcon")
	void setIcon(BufferedImage icon);
}
