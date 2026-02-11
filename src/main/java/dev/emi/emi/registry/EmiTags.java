package dev.emi.emi.registry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.client.resources.IResourceManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.TagExclusions;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.runtime.EmiTagKey;
import dev.emi.emi.util.InheritanceMap;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

public class EmiTags {
	public static final InheritanceMap<EmiRegistryAdapter<?>> ADAPTERS_BY_CLASS = new InheritanceMap<>(Maps.newHashMap());
	public static final Map<TagKey.Type, EmiRegistryAdapter<?>> ADAPTERS_BY_REGISTRY = Maps.newHashMap();
	public static final ResourceLocation HIDDEN_FROM_RECIPE_VIEWERS = EmiPort.id("c", "hidden_from_recipe_viewers");
	public static final Map<TagKey<?>, ResourceLocation> MODELED_TAGS = Maps.newHashMap();
	private static final Map<Set<?>, List<EmiTagKey<?>>> CACHED_TAGS = Maps.newHashMap();
	private static final Map<EmiTagKey<?>, List<?>> TAG_VALUES = Maps.newHashMap();
	private static final Map<ResourceLocation, List<EmiTagKey<?>>> SORTED_TAGS = Maps.newHashMap();
	public static final List<EmiTagKey<?>> TAGS = Lists.newArrayList();
	public static TagExclusions exclusions = new TagExclusions();

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> List<EmiStack> getValues(EmiTagKey<T> key) {
		if (TAG_VALUES.containsKey(key)) {
			EmiRegistryAdapter adapter = ADAPTERS_BY_REGISTRY.get(key.registry());
			if (adapter != null) {
				List<T> values = (List<T>) TAG_VALUES.getOrDefault(key, com.rewindmc.retroemi.shim.java.List.of());
				return values.stream().map(t -> adapter.of(t, EmiPort.emptyExtraData(), 1)).collect(Collectors.toList());
			}
		}
		return com.rewindmc.retroemi.shim.java.List.of();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> List<EmiStack> getRawValues(EmiTagKey<T> key) {
		if (key.isOf(EmiPort.getBlockRegistry())) {
			return key.stream().map(e -> EmiStack.of((Block) e)).collect(Collectors.toList());
		}
		EmiRegistryAdapter adapter = ADAPTERS_BY_REGISTRY.get(key.registry());
		if (adapter != null) {
			return key.stream().map(t -> adapter.of(t, EmiPort.emptyExtraData(), 1)).collect(Collectors.toList());
		}
		return com.rewindmc.retroemi.shim.java.List.of();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> EmiIngredient getIngredient(Class<T> clazz, List<EmiStack> stacks, long amount) {
		Map<T, EmiStack> map = Maps.newHashMap();
		for (EmiStack stack : stacks) {
			if (!stack.isEmpty()) {
				EmiStack existing = map.getOrDefault(stack.getKey(), null);
				if (existing != null && !stack.equals(existing)) {
					return new ListEmiIngredient(stacks, amount);
				}
				map.put((T) stack.getKey(), stack);
			}
		}
		if (map.size() == 0) {
			return EmiStack.EMPTY;
		} else if (map.size() == 1) {
			return map.values().stream().collect(Collectors.toList()).get(0).copy().setAmount(amount);
		}
		EmiRegistryAdapter<T> adapter = (EmiRegistryAdapter<T>) ADAPTERS_BY_CLASS.get(clazz);
		if (adapter == null) {
			return new ListEmiIngredient(stacks, amount);
		}
        TagKey. Type registry = adapter.getRegistry();
		List<EmiTagKey<T>> keys = (List<EmiTagKey<T>>) (List) CACHED_TAGS.get(map.keySet());

		if (keys != null) {
			for (EmiTagKey<T> key : keys) {
				List<T> values = key.getList();
				values.forEach(map::remove);
			}
		} else {
			keys = Lists.newArrayList();
			Set<T> original = new HashSet<>(map.keySet());
			for (EmiTagKey<T> key : (List<EmiTagKey<T>>) (List) getTags(registry)) {
				List<T> values = key.getList();
				if (values.size() < 2) {
					continue;
				}
				if (map.keySet().containsAll(values)) {
					values.forEach(map::remove);
					keys.add(key);
				}
				if (map.isEmpty()) {
					break;
				}
			}
			CACHED_TAGS.put((Set) original, (List) keys);
		}

		if (keys == null || keys.isEmpty()) {
			return new ListEmiIngredient(stacks.stream().collect(Collectors.toList()), amount);
		} else if (map.isEmpty()) {
			if (keys.size() == 1) {
				return tagIngredient(keys.get(0), amount);
			} else {
				return new ListEmiIngredient(keys.stream().map(k -> tagIngredient(k, 1)).collect(Collectors.toList()), amount);
			}
		} else {
			return new ListEmiIngredient(com.rewindmc.retroemi.shim.java.List.of(map.values().stream().map(i -> i.copy().setAmount(1)).collect(Collectors.toList()),
					keys.stream().map(k -> tagIngredient(k, 1)).collect(Collectors.toList()))
				.stream().flatMap(a -> a.stream()).collect(Collectors.toList()), amount);
		}
	}

	private static EmiIngredient tagIngredient(EmiTagKey<?> key, long amount) {
		List<?> list = TAG_VALUES.get(key);
		if (list == null || list.isEmpty()) {
			return EmiStack.EMPTY;
		} else if (list.size() == 1) {
			return new TagEmiIngredient(key, amount).getEmiStacks().get(0).copy().setAmount(amount);
		} else {
			return new TagEmiIngredient(key, amount);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> List<EmiTagKey<T>> getTags(TagKey.Type registry) {
		return (List<EmiTagKey<T>>) (List) SORTED_TAGS.getOrDefault(registry.getRegistryName(), com.rewindmc.retroemi.shim.java.List.of());
	}

	public static void registerTagModels(IResourceManager manager, Consumer<ResourceLocation> consumer) {
		EmiTags.MODELED_TAGS.clear();
		for (ResourceLocation id : EmiPort.findResources(manager, "models/tag/item", s -> s.endsWith(".json"))) {
            System.out.println("Registering tag model: " + id);
            String path = id.getResourcePath();
			path = path.substring(11, path.length() - 5);
			String[] parts = path.split("/");
			if (parts.length > 1) {
				TagKey<?> key = TagKey.of(TagKey.Type.of(EmiPort.id("minecraft", parts[0])), EmiPort.id(id.getResourceDomain(), path.substring(1 + parts[0].length())));
				ResourceLocation mid = EmiPort.id(id.getResourceDomain(), "tag/" + path);
				EmiTags.MODELED_TAGS.put(key, mid);
				consumer.accept(mid);
			}
		}
		/*
		Disable legacy tag models in 1.21+ due to modeling complications
		for (Identifier id : EmiPort.findResources(manager, "models/item/tags", s -> s.endsWith(".json"))) {
			String path = id.getPath();
			path = path.substring(0, path.length() - 5);
			String[] parts = path.substring(17).split("/");
			if (id.getNamespace().equals("emi") && parts.length > 1) {
				Identifier mid = new ModelIdentifier(id.getNamespace(), path.substring(12), "inventory");
				EmiTags.MODELED_TAGS.put(TagKey.of(EmiPort.getItemRegistry().getKey(), EmiPort.id(parts[0], path.substring(18 + parts[0].length()))), mid);
				consumer.accept(mid);
			}
		}
		*/
	}

	public static void reload() {
		EmiTagKey.reload();
		TAGS.clear();
		SORTED_TAGS.clear();
		TAG_VALUES.clear();
		CACHED_TAGS.clear();
		for (TagKey.Type registry : ADAPTERS_BY_REGISTRY.keySet()) {
			reloadTags(registry);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T> void reloadTags(TagKey.Type registry) {
		Set<T> hidden = com.rewindmc.retroemi.shim.java.Set.copyOf(((EmiTagKey<T>) EmiTagKey.of(registry, HIDDEN_FROM_RECIPE_VIEWERS)).getAll());
		ResourceLocation rid = registry.getRegistryName();
		List<EmiTagKey<T>> tags = registry.getAll().stream().map(key -> (TagKey<T>) key).map(EmiTagKey::of)
			.filter(key -> !exclusions.contains(rid, key.id()) && !hidden.containsAll(key.getAll()))
			.collect(Collectors.toList());
		logUntranslatedTags(tags);
		tags = consolodateTags(tags);
		for (EmiTagKey<T> key : tags) {
			List<T> values = key.stream().filter(s -> !EmiHidden.isDisabled(stackFromKey(key, s))).collect(Collectors.toList());
			if (values.isEmpty()) {
				TAG_VALUES.put(key, key.getList());
			} else {
				TAG_VALUES.put(key, values);
			}
		}
		EmiTags.TAGS.addAll(tags.stream().sorted((a, b) -> a.id().toString().compareTo(b.id().toString())).collect(Collectors.toList()));
		tags = tags.stream()
			.sorted((a, b) -> Long.compare(b.stream().count(), a.stream().count()))
			.collect(Collectors.toList());
		EmiTags.SORTED_TAGS.put(registry.getRegistryName(), (List) tags);
	}

	@SuppressWarnings("unchecked")
	private static <T> EmiStack stackFromKey(EmiTagKey<T> key, T t) {
		EmiRegistryAdapter<T> adapter = (EmiRegistryAdapter<T>) ADAPTERS_BY_REGISTRY.get(key.registry());
		if (adapter != null) {
			return adapter.of(t, EmiPort.emptyExtraData(), 1);
		}
		throw new UnsupportedOperationException("Unsupported tag registry " + key);
	}

	private static <T> void logUntranslatedTags(List<EmiTagKey<T>> tags) {
		if (EmiConfig.logUntranslatedTags) {
			List<String> untranslated = Lists.newArrayList();
			for (EmiTagKey<T> tag : tags) {
				if (!tag.hasTranslation()) {
					untranslated.add(tag.id().toString());
				}
			}
			if (!untranslated.isEmpty()) {
				for (String tag : untranslated.stream().sorted().collect(Collectors.toList())) {
					EmiReloadLog.warn("Untranslated tag #" + tag);
				}
				EmiReloadLog.info(" Tag warning can be disabled in the config, EMI docs describe how to add a translation or exclude tags.");
			}
		}
	}

	private static <T> List<EmiTagKey<T>> consolodateTags(List<EmiTagKey<T>> tags) {
		Map<Set<T>, EmiTagKey<T>> map = Maps.newHashMap();
		for (int i = 0; i < tags.size(); i++) {
			EmiTagKey<T> key = tags.get(i);
			Set<T> values = key.getSet();
			EmiTagKey<T> original = map.get(values);
			if (original != null) {
				map.put(values, betterTag(key, original));
			} else {
				map.put(values, key);
			}
		}
		return map.values().stream().collect(Collectors.toList());
	}

	private static<T> EmiTagKey<T> betterTag(EmiTagKey<T> a, EmiTagKey<T> b) {
		if (a.hasTranslation() != b.hasTranslation()) {
			return a.hasTranslation() ? a : b;
		}
		if (a.hasCustomModel() != b.hasCustomModel()) {
			return a.hasCustomModel() ? a : b;
		}
		String an = a.id().getResourceDomain();
		String bn = b.id().getResourceDomain();
		if (!an.equals(bn)) {
			if (an.equals("minecraft")) {
				return a;
			} else if (bn.equals("minecraft")) {
				return b;
			} else if (an.equals("c")) {
				return a;
			} else if (bn.equals("c")) {
				return b;
			} else if (an.equals("fabric")) {
				return EmiAgnos.isModLoaded("forge") ? b : a;
			} else if (bn.equals("fabric")) {
				return EmiAgnos.isModLoaded("forge") ? a : b;
			} else if (an.equals("forge")) {
				return EmiAgnos.isModLoaded("forge") ? a : b;
			} else if (bn.equals("forge")) {
				return EmiAgnos.isModLoaded("forge") ? b : a;
			}
		}
		return a.id().toString().length() <= b.id().toString().length() ? a : b;
	}
}
