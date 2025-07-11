package net.minecraft.client.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Provides an efficient way to search for a text in multiple texts.
 */
@SideOnly(Side.CLIENT)
public class SuffixArray<T> {
	private static final boolean PRINT_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
	private static final boolean PRINT_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
	private static final Logger LOGGER = Logger.getLogger("SuffixArray");
	private static final int END_OF_TEXT_MARKER = -1;
	private static final int END_OF_DATA = -2;
	protected final List<T> objects = Lists.<T>newArrayList();
	private final IntList characters = new IntArrayList();
	private final IntList textStarts = new IntArrayList();
	private IntList suffixIndexToObjectIndex = new IntArrayList();
	private IntList offsetInText = new IntArrayList();
	private int maxTextLength;

	/**
	 * Adds a text with the corresponding object.
	 *
	 * <p>You are not allowed to call this method after calling {@link #build()} method.
	 *
	 * <p>Takes O({@code text.length()}) time.
	 */
	public void add(T object, String text) {
		this.maxTextLength = Math.max(this.maxTextLength, text.length());
		int i = this.objects.size();
		this.objects.add(object);
		this.textStarts.add(this.characters.size());

		for(int j = 0; j < text.length(); ++j) {
			this.suffixIndexToObjectIndex.add(i);
			this.offsetInText.add(j);
			this.characters.add((int)text.charAt(j));
		}

		this.suffixIndexToObjectIndex.add(i);
		this.offsetInText.add(text.length());
		this.characters.add(-1);
	}

	/**
	 * Builds a suffix array with added texts.
	 *
	 * <p>You are not allowed to call this method multiple times.
	 *
	 * <p>Takes O(N * log N * log M) time on average where N is the sum of all text
	 * length added, and M is the maximum text length added.
	 */
	public void build() {
		int i = this.characters.size();
		int[] is = new int[i];
		int[] js = new int[i];
		int[] ks = new int[i];
		int[] ls = new int[i];
		IntComparator intComparator = (ix, jx) -> js[ix] == js[jx] ? Integer.compare(ks[ix], ks[jx]) : Integer.compare(js[ix], js[jx]);
		Swapper swapper = (ix, jx) -> {
			if (ix != jx) {
				int kx = js[ix];
				js[ix] = js[jx];
				js[jx] = kx;
				kx = ks[ix];
				ks[ix] = ks[jx];
				ks[jx] = kx;
				kx = ls[ix];
				ls[ix] = ls[jx];
				ls[jx] = kx;
			}

		};

		for(int j = 0; j < i; ++j) {
			is[j] = this.characters.getInt(j);
		}

		int j = 1;

		for(int k = Math.min(i, this.maxTextLength); j * 2 < k; j *= 2) {
			for(int l = 0; l < i; ls[l] = l++) {
				js[l] = is[l];
				ks[l] = l + j < i ? is[l + j] : -2;
			}

			Arrays.quickSort(0, i, intComparator, swapper);

			for(int l = 0; l < i; ++l) {
				if (l > 0 && js[l] == js[l - 1] && ks[l] == ks[l - 1]) {
					is[ls[l]] = is[ls[l - 1]];
				} else {
					is[ls[l]] = l;
				}
			}
		}

		IntList intList = this.suffixIndexToObjectIndex;
		IntList intList2 = this.offsetInText;
		this.suffixIndexToObjectIndex = new IntArrayList(intList.size());
		this.offsetInText = new IntArrayList(intList2.size());

		for(int m = 0; m < i; ++m) {
			int n = ls[m];
			this.suffixIndexToObjectIndex.add(intList.getInt(n));
			this.offsetInText.add(intList2.getInt(n));
		}

		if (PRINT_ARRAY) {
			this.printArray();
		}

	}

	private void printArray() {
		for(int i = 0; i < this.suffixIndexToObjectIndex.size(); ++i) {
			LOGGER.info(i + this.getDebugString(i));
		}

		LOGGER.info("");
	}

	private String getDebugString(int suffixIndex) {
		int i = this.offsetInText.getInt(suffixIndex);
		int j = this.textStarts.getInt(this.suffixIndexToObjectIndex.getInt(suffixIndex));
		StringBuilder stringBuilder = new StringBuilder();

		for(int k = 0; j + k < this.characters.size(); ++k) {
			if (k == i) {
				stringBuilder.append('^');
			}

			int l = this.characters.getInt(j + k);
			if (l == -1) {
				break;
			}

			stringBuilder.append((char)l);
		}

		return stringBuilder.toString();
	}

	private int compare(String string, int suffixIndex) {
		int i = this.textStarts.getInt(this.suffixIndexToObjectIndex.getInt(suffixIndex));
		int j = this.offsetInText.getInt(suffixIndex);

		for(int k = 0; k < string.length(); ++k) {
			int l = this.characters.getInt(i + j + k);
			if (l == -1) {
				return 1;
			}

			char c = string.charAt(k);
			char d = (char)l;
			if (c < d) {
				return -1;
			}

			if (c > d) {
				return 1;
			}
		}

		return 0;
	}

	/**
	 * Retrieves all objects of which corresponding texts contain {@code text}.
	 *
	 * <p>You have to call {@link #build()} method before calling this method.
	 *
	 * <p>Takes O({@code text.length()} * log N) time to find objects where N is the
	 * sum of all text length added. Takes O(X + Y * log Y) time to collect found
	 * objects into a list where X is the number of occurrences of {@code text} in all
	 * texts added, and Y is the number of found objects.
	 */
	public List<T> findAll(String text) {
		int i = this.suffixIndexToObjectIndex.size();
		int j = 0;
		int k = i;

		while(j < k) {
			int l = j + (k - j) / 2;
			int m = this.compare(text, l);
			if (PRINT_COMPARISONS) {
				LOGGER.info("comparing lower \""+text+ "\" with "+l+" \""+ this.getDebugString(l) + "\": " + m +"");
			}

			if (m > 0) {
				j = l + 1;
			} else {
				k = l;
			}
		}

		if (j >= 0 && j < i) {
			int l = j;
			k = i;

			while(j < k) {
				int m = j + (k - j) / 2;
				int n = this.compare(text, m);
				if (PRINT_COMPARISONS) {
					LOGGER.info("comparing upper \""+ text +"\" with "+ m +" \"" + this.getDebugString(m) + "\": " + n);
				}

				if (n >= 0) {
					j = m + 1;
				} else {
					k = m;
				}
			}

			int m = j;
			IntSet intSet = new IntOpenHashSet();

			for(int o = l; o < m; ++o) {
				intSet.add(this.suffixIndexToObjectIndex.getInt(o));
			}

			int[] is = intSet.toIntArray();
			java.util.Arrays.sort(is);
			Set<T> set = Sets.<T>newLinkedHashSet();

			for(int p : is) {
				set.add(this.objects.get(p));
			}

			return Lists.<T>newArrayList(set);
		} else {
			return Collections.emptyList();
		}
	}
}
