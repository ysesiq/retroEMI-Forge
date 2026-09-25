package com.rewindmc.retroemi.plugin;

import java.util.Map;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("RetroEMI")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class REMILoadingPlugin implements IFMLLoadingPlugin {

//	public REMILoadingPlugin() {
//		MixinBootstrap.init();
//		Mixins.addConfiguration("emi.mixins.json");
//	}

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
}
