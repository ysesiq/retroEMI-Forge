package dev.emi.emi.mixin.accessor;

import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ResourcePackRepository.Entry.class)
public interface ResourcePackRepositoryEntryAccessor {
	@Accessor("rePackMetadataSection")
	void setMetadata(PackMetadataSection meta);

	@Accessor("locationTexturePackIcon")
	void setIcon(ResourceLocation icon);
}
