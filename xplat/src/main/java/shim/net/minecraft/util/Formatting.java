package shim.net.minecraft.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Wrap TextFormatting to facilitate multi-version synchronization
 */
public enum Formatting {
	BLACK("BLACK", '0', 0, 0x000000),
	DARK_BLUE("DARK_BLUE", '1', 1, 0x0000AA),
	DARK_GREEN("DARK_GREEN", '2', 2, 0x00AA00),
	DARK_AQUA("DARK_AQUA", '3', 3, 0x00AAAA),
	DARK_RED("DARK_RED", '4', 4, 0xAA0000),
	DARK_PURPLE("DARK_PURPLE", '5', 5, 0xAA00AA),
	GOLD("GOLD", '6', 6, 0xFFAA00),
	GRAY("GRAY", '7', 7, 0xAAAAAA),
	DARK_GRAY("DARK_GRAY", '8', 8, 0x555555),
	BLUE("BLUE", '9', 9, 0x5555FF),
	GREEN("GREEN", 'a', 10, 0x55FF55),
	AQUA("AQUA", 'b', 11, 0x55FFFF),
	RED("RED", 'c', 12, 0xFF5555),
	LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, 0xFF55FF),
	YELLOW("YELLOW", 'e', 14, 0x55FFFF),
	WHITE("WHITE", 'f', 15, 0xFFFFFF),
	OBFUSCATED("OBFUSCATED", 'k', true),
	BOLD("BOLD", 'l', true),
	STRIKETHROUGH("STRIKETHROUGH", 'm', true),
	UNDERLINE("UNDERLINE", 'n', true),
	ITALIC("ITALIC", 'o', true),
	RESET("RESET", 'r', -1, null);

	public static final char FORMATTING_CODE_PREFIX = '§';
	private static final Map<String, Formatting> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((f) -> sanitize(f.name), (f) -> f));
	private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
	private final String name;
	private final char code;
	private final boolean modifier;
	private final String stringValue;
	private final int colorIndex;
	@Nullable
	private final Integer colorValue;

	private static String sanitize(String name) {
		return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
	}

	Formatting(final String name, final char code, final int colorIndex, @Nullable final Integer colorValue) {
		this(name, code, false, colorIndex, colorValue);
	}

	Formatting(final String name, final char code, final boolean modifier) {
		this(name, code, modifier, -1, null);
	}

	Formatting(final String name, final char code, final boolean modifier, final int colorIndex, @Nullable final Integer colorValue) {
		this.name = name;
		this.code = code;
		this.modifier = modifier;
		this.colorIndex = colorIndex;
		this.colorValue = colorValue;
		this.stringValue = String.valueOf(FORMATTING_CODE_PREFIX) + code;
	}

	public char getCode() {
		return this.code;
	}

	public int getColorIndex() {
		return this.colorIndex;
	}

	public boolean isModifier() {
		return this.modifier;
	}

	public boolean isColor() {
		return !this.modifier && this != RESET;
	}

	@Nullable
	public Integer getColorValue() {
		return this.colorValue;
	}

	public String getName() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	public String toString() {
		return this.stringValue;
	}

	@Nullable
	@Contract("!null->!null;_->_")
	public static String strip(@Nullable String string) {
		return string == null ? null : FORMATTING_CODE_PATTERN.matcher(string).replaceAll("");
	}

	@Nullable
	public static Formatting byName(@Nullable String name) {
		return name == null ? null : BY_NAME.get(sanitize(name));
	}

	public static Formatting wrap(TextFormatting raw) {
		return Formatting.valueOf(raw.name());
	}

	public TextFormatting raw() {
		return TextFormatting.valueOf(this.name);
	}
}
