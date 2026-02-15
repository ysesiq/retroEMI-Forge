package shim.java;

import com.google.common.collect.Lists;

import java.util.Collections;

public class List {

	public static <E> java.util.List<E> of() {
		return Collections.emptyList();
	}

	public static <E> java.util.List<E> of(E e) {
		return Collections.singletonList(e);
	}

	@SafeVarargs
	public static <E> java.util.List<E> of(E... e) {
		return Collections.unmodifiableList(Lists.newArrayList(e));
	}

	public static <E> java.util.List<E> copyOf(Iterable<E> iter) {
		return Collections.unmodifiableList(Lists.newArrayList(iter));
	}

}
