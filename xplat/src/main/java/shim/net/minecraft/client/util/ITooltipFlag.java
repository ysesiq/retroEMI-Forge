package shim.net.minecraft.client.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ITooltipFlag {

	@SideOnly(Side.CLIENT)
	public static enum TooltipFlags {
		NORMAL(false),
		ADVANCED(true);

		final boolean isAdvanced;

		private TooltipFlags(boolean advanced) {
			this.isAdvanced = advanced;
		}

		public boolean isAdvanced() {
			return this.isAdvanced;
		}
	}
}
