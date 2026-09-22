package shim.java;

import com.google.common.collect.Sets;

import java.util.Collections;

public class Set {

	public static <E> java.util.Set<E> of() {
		return Collections.emptySet();
	}

	public static <E> java.util.Set<E> of(E e) {
		return Collections.singleton(e);
	}

	@SafeVarargs
	public static <E> java.util.Set<E> of(E... e) {
		return Collections.unmodifiableSet(Sets.newLinkedHashSet());
	}

	public static <E> java.util.Set<E> copyOf(Iterable<E> iter) {
		return Collections.unmodifiableSet(Sets.newLinkedHashSet(iter));
	}

}
