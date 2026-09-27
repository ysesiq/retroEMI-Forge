package dev.emi.emi.registry;

import java.util.Collections;
import java.util.Map;

import dev.emi.emi.api.stack.Comparison;
import shim.net.minecraft.registry.tag.ItemKey;

public class EmiComparisonDefaults {
	public static Map<Object, Comparison> comparisons = Collections.emptyMap();

	public static Comparison get(Object obj) {
		if (comparisons.containsKey(obj)) {
			return comparisons.get(obj);
		}
		if (obj instanceof ItemKey key && comparisons.containsKey(key.item())) {
			return comparisons.get(key.item());
		}
		return Comparison.DEFAULT_COMPARISON;
	}
}
