package dev.emi.emi.mixinsupport;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

@IFMLLoadingPlugin.Name("REMIEarlyMixins")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class REMIEarlyMixins implements IFMLLoadingPlugin {

	public REMIEarlyMixins() {
		MixinBootstrap.init();
		Mixins.addConfiguration("emi.mixins.json");
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
}
