package dev.emi.emi.mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import dev.emi.emi.runtime.EmiLog;


@IFMLLoadingPlugin.Name("REMIEarlyMixins")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class REMIEarlyMixins implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private String[] transformerClasses;

    @Override
    public String getMixinConfig() {
        return "mixins.emi.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        List<String> mixins = new ArrayList<>();
        if(!FMLLaunchHandler.side().isClient()) {
            return mixins;
        }

        final List<String> notLoading = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.EARLY) {
                if (mixin.shouldLoad(loadedCoreMods, Collections.emptySet())) {
                    mixins.addAll(mixin.mixinClasses);
                } else {
                    notLoading.addAll(mixin.mixinClasses);
                }
            }
        }
        EmiLog.LOG.info("Not loading the following EARLY mixins: {}", notLoading);

        return mixins;
    }

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

}
