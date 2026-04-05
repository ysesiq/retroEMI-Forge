package dev.emi.emi.mixin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Locale.class)
public abstract class LocaleMixin {
    @Shadow Map<String, String> properties;

    @Inject(method = "translateKeyPrivate", at = @At(value = "HEAD"), cancellable = true)
    private void betterTranslation(String registerName, CallbackInfoReturnable<String> cir) {
        String localeTranslation = this.properties.get(registerName);
        if (localeTranslation != null) {
            cir.setReturnValue(localeTranslation);
        } else {
            String statTranslation = I18n.translateToLocal(registerName);
            cir.setReturnValue((statTranslation != null ? statTranslation : registerName));
        }
    }

    @Inject(method = "loadLocaleDataFiles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/Locale;checkUnicode()V"))
    private void readJsonFile(IResourceManager resourceManager, List<String> langList, CallbackInfo ci) {
        for (String localeName : langList) {
            String filePath = String.format("lang/%s.json", localeName);
            try {
                ResourceLocation resourceLocation = EmiPort.id("emi", filePath);
                List<IResource> resources = resourceManager.getAllResources(resourceLocation);
                if (!resources.isEmpty()) {
                    this.loadJsonFile(resourceManager, filePath);
                }
            } catch (Exception ignore) {
            }
        }
    }

    @Unique
    private void loadJsonFile(IResourceManager resourceManager, String fileName) {
        for (String resourceDomain : resourceManager.getResourceDomains()) {
            try {
                this.loadJsonData(resourceManager.getAllResources(EmiPort.id(resourceDomain, fileName)), fileName);
            } catch (Exception ignore) {
            }
        }
    }

    @Unique
    private void loadJsonData(List<IResource> resources, String fileName) {
        Gson gson = new Gson();
        for (IResource resource : resources) {
            try (InputStream stream = resource.getInputStream(); InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                jsonObject.entrySet().forEach(x -> this.properties.put(x.getKey(), (x.getValue()).getAsString()));
            } catch (JsonIOException | JsonSyntaxException | IOException e) {
                EmiLog.error("Exception when reading lang file: '" + fileName + "'", e);
            }
        }
    }
}
