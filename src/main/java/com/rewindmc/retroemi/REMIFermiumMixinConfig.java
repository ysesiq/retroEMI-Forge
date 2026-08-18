package com.rewindmc.retroemi;

import fermiumbooter.annotations.MixinConfig;
import net.minecraftforge.common.config.Config;

@MixinConfig(name = "emi")
public class REMIFermiumMixinConfig {

	@Config.Name("Enable EMI Early Mixins")
	@MixinConfig.MixinToggle(earlyMixin = "emi.mixins.json", defaultValue = true)
	public static boolean emiMixin = true;

	@Config.Name("Enable JEI Late Mixins")
	@MixinConfig.MixinToggle(lateMixin = "emi.jei.mixins.json", defaultValue = true)
	public static boolean emiJeiMixin = true;
}
