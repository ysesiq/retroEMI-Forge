package com.rewindmc.retroemi.plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("RetroEMI")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class REMILoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

	public REMILoadingPlugin() {
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[0];
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
	@Override
	public String getMixinConfig() {
		return "emi.mixins.json";
	}

	@Override
	public List<String> getMixins(Set<String> loadedCoreMods) {
		return shim.java.List.of();
	}
}
