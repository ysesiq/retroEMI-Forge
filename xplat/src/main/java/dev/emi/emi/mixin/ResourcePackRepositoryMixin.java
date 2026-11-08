package dev.emi.emi.mixin;

import com.rewindmc.retroemi.client.EmiJappaResourcePack;
import dev.emi.emi.mixin.accessor.ResourcePackRepositoryEntryAccessor;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ResourcePackRepository.class)
public class ResourcePackRepositoryMixin {
	@Shadow private List<ResourcePackRepository.Entry> repositoryEntriesAll;

	@Unique private static EmiJappaResourcePack emiJappaPack;

	@Inject(method = "updateRepositoryEntriesAll", at =@ At(value = "RETURN"))
	private void addEmiModernityPack(CallbackInfo callback) {
		if (emiJappaPack == null) {
			emiJappaPack = new EmiJappaResourcePack();
		}
		ResourcePackRepository.Entry entry = ((ResourcePackRepository) (Object) this).new Entry(emiJappaPack);
		ResourcePackRepositoryEntryAccessor accessor = (ResourcePackRepositoryEntryAccessor) entry;
		accessor.setMetadata(new PackMetadataSection(new TextComponentString("Modernity textures for retroEMI Forge"), 3));
		accessor.setIcon(new ResourceLocation("emi", "icon.png"));
		this.repositoryEntriesAll.add(entry);
	}
}
