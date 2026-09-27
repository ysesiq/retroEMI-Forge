package dev.emi.emi.mixin;

import com.gtnewhorizon.gtnhlib.client.model.unbaked.JSONModel;
import com.gtnewhorizon.gtnhlib.client.renderer.cel.model.quad.ModelQuadViewMutable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static org.joml.Math.fma;

@Mixin(JSONModel.class)
public class JSONModelMixin {
	/**
	 * @author Xy_Luce
	 * @reason Support bake item sprite
	 */
	@Overwrite(remap = false)
	protected void bakeSprite(ModelQuadViewMutable quad, String name) {
		name = name.replaceFirst("^minecraft:", "");
		TextureMap blocks = Minecraft.getMinecraft().getTextureMapBlocks();
		var icon = blocks.getAtlasSprite(name);
		if ("missingno".equals(icon.getIconName())) {
			icon = ((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationItemsTexture)).getAtlasSprite(name);
		}
		final float minU = icon.getMinU();
		final float minV = icon.getMinV();
		final float dU = icon.getMaxU() - minU;
		final float dV = icon.getMaxV() - minV;
		quad.setSprite(icon);

		for (int i = 0; i < 4; ++i) {
			quad.setTexU(i, fma(dU, quad.getTexU(i) / 16, minU));
			quad.setTexV(i, fma(dV, quad.getTexV(i) / 16, minV));
		}
	}
}
