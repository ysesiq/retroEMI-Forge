package shim.net.minecraft.resource;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ResourceReloader {
	void reload();

	default String getName() {
		return this.getClass().getSimpleName();
	}
}
