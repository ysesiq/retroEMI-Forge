package dev.emi.emi.mixin;

import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;


@IFMLLoadingPlugin.Name("REMIEarlyMixins")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class REMIEarlyMixins implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private String[] transformerClasses;

    @Override
    public String[] getASMTransformerClass() {
        if (transformerClasses == null) {
//            Namer.initNames();
//            transformerClasses = AsmTransformers.getTransformers();
        }
        return transformerClasses;
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
        return List.of("mixins.emi.json", "mixins.emi.early.json", "mixins.emi.late.json");
    }
}
