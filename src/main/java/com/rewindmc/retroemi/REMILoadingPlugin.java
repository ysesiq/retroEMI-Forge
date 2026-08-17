package com.rewindmc.retroemi;

import java.util.List;
import java.util.Map;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

@IFMLLoadingPlugin.Name("RetroEMI")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class REMILoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

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
	public List<String> getMixinConfigs() {
		return shim.java.List.of("emi.mixins.json");
	}
}
