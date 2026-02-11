package dev.emi.emi.runtime;

import com.google.common.collect.Maps;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.registry.EmiTags;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Wrapper around TagKeys
public class EmiTagKey<T> {
    public static final Map<TagKey<?>, EmiTagKey<?>> CACHE = Maps.newHashMap();
    private final TagKey<T> raw;
    private List<T> cached;

    private EmiTagKey(TagKey<T> raw) {
        this.raw = raw;
        recalculate();
    }

    public void recalculate() {
        cached = stream().collect(Collectors.toList());
    }

    public TagKey<T> raw() {
        return raw;
    }

    public boolean isOf(RegistryNamespaced registry) {
        return raw.isOf(registry);
    }

    public ResourceLocation id() {
        return raw.id();
    }

    public TagKey.Type registry() {
        return raw.getType();
    }

    public Stream<T> stream() {
//        Registry<T> registry = registry();
//        Optional<Named<T>> opt = registry.getEntryList(raw);
//        if (opt.isEmpty()) {
//            return Stream.of();
//        } else {
//            if (registry == EmiPort.getFluidRegistry()) {
//                return opt.get().stream().filter(o -> {
//                    Fluid f = (Fluid) o.value();
//                    return f.isStill(f.getDefaultState());
//                }).map(RegistryEntry::value);
//            }
//            return opt.get().stream().map(RegistryEntry::value);
//        }
        return raw.getAll().stream();
    }

    public List<T> getList() {
        return cached;
    }

    public Set<T> getSet() {
        return stream().collect(Collectors.toSet());
    }

    public Text getTagName() {
        String s = getTagTranslationKey();
        if (s == null) {
            return EmiPort.literal("#" + this.id());
        } else {
            return EmiPort.translatable(s);
        }
    }

    public boolean hasTranslation() {
        return getTagTranslationKey() != null;
    }

    private @Nullable String getTagTranslationKey() {
        ResourceLocation registry = raw.registry();
        if (registry.getResourceDomain().equals("minecraft")) {
            String s = translatePrefix("tag." + registry.getResourcePath().replace("/", ".") + ".", this.id());
            if (s != null) {
                return s;
            }
        } else {
            String s = translatePrefix("tag." + registry.getResourceDomain() + "." + registry.getResourcePath().replace("/", ".") + ".", this.id());
            if (s != null) {
                return s;
            }
        }
        return translatePrefix("tag.", this.id());
    }

    private static @Nullable String translatePrefix(String prefix, ResourceLocation id) {
        String s = EmiUtil.translateId(prefix, id);
        if (I18n.hasTranslation(s)) {
            return s;
        }
        if (id.getResourceDomain().equals("forge")) {
            s = EmiUtil.translateId(prefix, EmiPort.id("c", id.getResourcePath()));
            if (I18n.hasTranslation(s)) {
                return s;
            }
        }
        return null;
    }

    public @Nullable ResourceLocation getCustomModel() {
        ResourceLocation rid = this.id();
        if (rid.getResourceDomain().equals("forge") && !EmiTags.MODELED_TAGS.containsKey(raw())) {
            return EmiTagKey.of(TagKey.of(raw.getType(), EmiPort.id("c", rid.getResourcePath()))).getCustomModel();
        }
        return EmiTags.MODELED_TAGS.get(raw());
    }

    public boolean hasCustomModel() {
        return getCustomModel() != null;
    }

    @Override
    public int hashCode() {
        return raw().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmiTagKey other && raw().equals(other.raw());
    }

    @SuppressWarnings("unchecked")
    public static <T> EmiTagKey<T> of(TagKey<T> raw) {
        return (EmiTagKey<T>) CACHE.computeIfAbsent(raw, EmiTagKey::new);
    }

    public static <T> EmiTagKey<T> of(TagKey.Type registry, ResourceLocation id) {
        return of(TagKey.of(registry, id));
    }

    public static <T> Stream<EmiTagKey<T>> fromRegistry(RegistryNamespaced registry) {
        return registry.getKeys().stream().map(tagKey -> of((TagKey<T>) tagKey));
    }

    public static void reload() {
        for (EmiTagKey<?> key : CACHE.values()) {
            key.recalculate();
        }
    }

    public List<T> getAll() {
        return raw.getAll();
    }
}
