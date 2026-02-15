package shim.java;

import java.util.Collections;
import java.util.HashMap;

public class Map {

	public static <K, V> java.util.Map<K, V> of() {
		return Collections.emptyMap();
	}

	public static <K, V> java.util.Map<K, V> of(K k, V v) {
		return Collections.singletonMap(k, v);
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <K extends T, V extends T, T> java.util.Map<K, V> of(T... vals) {
		var m = new HashMap<K, V>();
		for (int i = 0; i < vals.length; i += 2) {
			m.put((K)vals[i], (V)vals[i+1]);
		}
		return Collections.unmodifiableMap(m);
	}

	public static <K, V> java.util.Map<K, V> copyOf(java.util.Map<K, V> m) {
		return Collections.unmodifiableMap(new HashMap<>(m));
	}

}
