package shim.net.minecraft.resource;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ResourceReloader {
	void reload();

	default String getName() {
		return this.getClass().getSimpleName();
	}
}
