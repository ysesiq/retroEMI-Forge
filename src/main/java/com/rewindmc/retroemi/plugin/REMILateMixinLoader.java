package com.rewindmc.retroemi.plugin;

import java.util.Collections;
import java.util.List;

import zone.rong.mixinbooter.ILateMixinLoader;

public class REMILateMixinLoader implements ILateMixinLoader {
	@Override
	public List<String> getMixinConfigs() {
		return Collections.singletonList("emi.jei.mixins.json");
	}

	@Override
	public boolean shouldMixinConfigQueue(String mixinConfig) {
		return true;
	}
}
