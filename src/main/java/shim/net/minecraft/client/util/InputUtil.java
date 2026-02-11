package shim.net.minecraft.client.util;

import com.google.common.collect.Maps;
import net.minecraft.util.StringTranslate;
import shim.net.minecraft.text.Text;
import shim.org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiFunction;

public class InputUtil {
	public static final Key UNKNOWN_KEY = Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_UNKNOWN);

	public static Key fromKeyCode(int keyCode, int scanCode) {
		return keyCode == GLFW.GLFW_KEY_UNKNOWN ? Type.SCANCODE.createFromCode(scanCode) : Type.KEYSYM.createFromCode(keyCode);
	}

	public static Key fromTranslationKey(String translationKey) {
		if (Key.KEYS_BY_ID.containsKey(translationKey)) {
			return (Key) Key.KEYS_BY_ID.get(translationKey);
		} else {
			for(Type type : Type.values()) {
				if (translationKey.startsWith(type.name)) {
					String string = translationKey.substring(type.name.length() + 1);
					int i = Integer.parseInt(string);
					if (type == Type.MOUSE) {
						--i;
					}

					return type.createFromCode(i);
				}
			}

			throw new IllegalArgumentException("Unknown key name: " + translationKey);
		}
	}

	public static boolean isKeyPressed(int code) {
		return Keyboard.isKeyDown(code);
	}

	public static final class Key {
		private final String translationKey;
		private final Type type;
		private final int keyCode;
		private final Text displayText;
		static final Map<String, Key> KEYS_BY_ID = Maps.newHashMap();

		Key(String translationKey, Type type, int keyCode) {
			this.translationKey = translationKey;
			this.type = type;
			this.keyCode = keyCode;
			this.displayText = type.displayTextCreator.apply(keyCode, translationKey);
			KEYS_BY_ID.put(translationKey, this);
		}

		public Type getCategory() {
			return this.type;
		}

		public int getCode() {
			return this.keyCode;
		}

		public String getTranslationKey() {
			return this.translationKey;
		}

		public Text getLocalizedText() {
			return this.displayText;
		}

		public OptionalInt toInt() {
			if (this.keyCode >= 48 && this.keyCode <= 57) {
				return OptionalInt.of(this.keyCode - 48);
			} else {
				return this.keyCode >= 320 && this.keyCode <= 329 ? OptionalInt.of(this.keyCode - 320) : OptionalInt.empty();
			}
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				Key key = (Key)object;
				return this.keyCode == key.keyCode && this.type == key.type;
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.type, this.keyCode});
		}

		public String toString() {
			return this.translationKey;
		}
	}

	public static enum Type {
		KEYSYM("key.keyboard", (keyCode, translationKey) -> {
			String string = Keyboard.getKeyName(keyCode);
			return string != null ? Text.literal(string) : Text.translatable(translationKey);
		}),
		SCANCODE("scancode", (scanCode, translationKey) -> {
			return Text.translatable(translationKey);
		}),
		MOUSE(
			"key.mouse",
			(buttonCode, translationKey) -> StringTranslate.getInstance().containsTranslateKey(translationKey)
					? Text.translatable(translationKey)
					: Text.translatable("key.mouseButton", buttonCode + 1)
		);

		private final Map<Integer, Key> keys = new HashMap<>();
		final String name;
		final BiFunction<Integer, String, Text> displayTextCreator;

		private static void mapKey(Type type, String translationKey, int keyCode) {
			Key key = new Key(translationKey, type, keyCode);
			type.keys.put(keyCode, key);
		}

		private Type(String name, BiFunction<Integer, String, Text> displayTextCreator) {
			this.name = name;
			this.displayTextCreator = displayTextCreator;
		}

		public Key createFromCode(int code) {
			return keys.computeIfAbsent(code, (i -> {
				if (this == MOUSE) i++;
				return new Key(name+"."+i, this, i);
			}));
		}

		static {
			// giant wall of mappings for compatibility with modern EMI versions (global config!)
			mapKey(KEYSYM, "key.keyboard.unknown", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(MOUSE, "key.mouse.left", GLFW.GLFW_MOUSE_BUTTON_LEFT);
			mapKey(MOUSE, "key.mouse.right", GLFW.GLFW_MOUSE_BUTTON_RIGHT);
			mapKey(MOUSE, "key.mouse.middle", GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
			mapKey(MOUSE, "key.mouse.4", GLFW.GLFW_MOUSE_BUTTON_4);
			mapKey(MOUSE, "key.mouse.5", GLFW.GLFW_MOUSE_BUTTON_5);
			mapKey(MOUSE, "key.mouse.6", GLFW.GLFW_MOUSE_BUTTON_6);
			mapKey(MOUSE, "key.mouse.7", GLFW.GLFW_MOUSE_BUTTON_7);
			mapKey(MOUSE, "key.mouse.8", GLFW.GLFW_MOUSE_BUTTON_8);
			mapKey(KEYSYM, "key.keyboard.0", GLFW.GLFW_KEY_0);
			mapKey(KEYSYM, "key.keyboard.1", GLFW.GLFW_KEY_1);
			mapKey(KEYSYM, "key.keyboard.2", GLFW.GLFW_KEY_2);
			mapKey(KEYSYM, "key.keyboard.3", GLFW.GLFW_KEY_3);
			mapKey(KEYSYM, "key.keyboard.4", GLFW.GLFW_KEY_4);
			mapKey(KEYSYM, "key.keyboard.5", GLFW.GLFW_KEY_5);
			mapKey(KEYSYM, "key.keyboard.6", GLFW.GLFW_KEY_6);
			mapKey(KEYSYM, "key.keyboard.7", GLFW.GLFW_KEY_7);
			mapKey(KEYSYM, "key.keyboard.8", GLFW.GLFW_KEY_8);
			mapKey(KEYSYM, "key.keyboard.9", GLFW.GLFW_KEY_9);
			mapKey(KEYSYM, "key.keyboard.a", GLFW.GLFW_KEY_A);
			mapKey(KEYSYM, "key.keyboard.b", GLFW.GLFW_KEY_B);
			mapKey(KEYSYM, "key.keyboard.c", GLFW.GLFW_KEY_C);
			mapKey(KEYSYM, "key.keyboard.d", GLFW.GLFW_KEY_D);
			mapKey(KEYSYM, "key.keyboard.e", GLFW.GLFW_KEY_E);
			mapKey(KEYSYM, "key.keyboard.f", GLFW.GLFW_KEY_F);
			mapKey(KEYSYM, "key.keyboard.g", GLFW.GLFW_KEY_G);
			mapKey(KEYSYM, "key.keyboard.h", GLFW.GLFW_KEY_H);
			mapKey(KEYSYM, "key.keyboard.i", GLFW.GLFW_KEY_I);
			mapKey(KEYSYM, "key.keyboard.j", GLFW.GLFW_KEY_J);
			mapKey(KEYSYM, "key.keyboard.k", GLFW.GLFW_KEY_K);
			mapKey(KEYSYM, "key.keyboard.l", GLFW.GLFW_KEY_L);
			mapKey(KEYSYM, "key.keyboard.m", GLFW.GLFW_KEY_M);
			mapKey(KEYSYM, "key.keyboard.n", GLFW.GLFW_KEY_N);
			mapKey(KEYSYM, "key.keyboard.o", GLFW.GLFW_KEY_O);
			mapKey(KEYSYM, "key.keyboard.p", GLFW.GLFW_KEY_P);
			mapKey(KEYSYM, "key.keyboard.q", GLFW.GLFW_KEY_Q);
			mapKey(KEYSYM, "key.keyboard.r", GLFW.GLFW_KEY_R);
			mapKey(KEYSYM, "key.keyboard.s", GLFW.GLFW_KEY_S);
			mapKey(KEYSYM, "key.keyboard.t", GLFW.GLFW_KEY_T);
			mapKey(KEYSYM, "key.keyboard.u", GLFW.GLFW_KEY_U);
			mapKey(KEYSYM, "key.keyboard.v", GLFW.GLFW_KEY_V);
			mapKey(KEYSYM, "key.keyboard.w", GLFW.GLFW_KEY_W);
			mapKey(KEYSYM, "key.keyboard.x", GLFW.GLFW_KEY_X);
			mapKey(KEYSYM, "key.keyboard.y", GLFW.GLFW_KEY_Y);
			mapKey(KEYSYM, "key.keyboard.z", GLFW.GLFW_KEY_Z);
			mapKey(KEYSYM, "key.keyboard.f1", GLFW.GLFW_KEY_F1);
			mapKey(KEYSYM, "key.keyboard.f2", GLFW.GLFW_KEY_F2);
			mapKey(KEYSYM, "key.keyboard.f3", GLFW.GLFW_KEY_F3);
			mapKey(KEYSYM, "key.keyboard.f4", GLFW.GLFW_KEY_F4);
			mapKey(KEYSYM, "key.keyboard.f5", GLFW.GLFW_KEY_F5);
			mapKey(KEYSYM, "key.keyboard.f6", GLFW.GLFW_KEY_F6);
			mapKey(KEYSYM, "key.keyboard.f7", GLFW.GLFW_KEY_F7);
			mapKey(KEYSYM, "key.keyboard.f8", GLFW.GLFW_KEY_F8);
			mapKey(KEYSYM, "key.keyboard.f9", GLFW.GLFW_KEY_F9);
			mapKey(KEYSYM, "key.keyboard.f10", GLFW.GLFW_KEY_F10);
			mapKey(KEYSYM, "key.keyboard.f11", GLFW.GLFW_KEY_F11);
			mapKey(KEYSYM, "key.keyboard.f12", GLFW.GLFW_KEY_F12);
			mapKey(KEYSYM, "key.keyboard.f13", GLFW.GLFW_KEY_F13);
			mapKey(KEYSYM, "key.keyboard.f14", GLFW.GLFW_KEY_F14);
			mapKey(KEYSYM, "key.keyboard.f15", GLFW.GLFW_KEY_F15);
			mapKey(KEYSYM, "key.keyboard.f16", GLFW.GLFW_KEY_F16);
			mapKey(KEYSYM, "key.keyboard.f17", GLFW.GLFW_KEY_F17);
			mapKey(KEYSYM, "key.keyboard.f18", GLFW.GLFW_KEY_F18);
			mapKey(KEYSYM, "key.keyboard.f19", GLFW.GLFW_KEY_F19);
			mapKey(KEYSYM, "key.keyboard.f20", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.f21", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.f22", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.f23", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.f24", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.f25", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.num.lock", GLFW.GLFW_KEY_NUM_LOCK);
			mapKey(KEYSYM, "key.keyboard.keypad.0", GLFW.GLFW_KEY_KP_0);
			mapKey(KEYSYM, "key.keyboard.keypad.1", GLFW.GLFW_KEY_KP_1);
			mapKey(KEYSYM, "key.keyboard.keypad.2", GLFW.GLFW_KEY_KP_2);
			mapKey(KEYSYM, "key.keyboard.keypad.3", GLFW.GLFW_KEY_KP_3);
			mapKey(KEYSYM, "key.keyboard.keypad.4", GLFW.GLFW_KEY_KP_4);
			mapKey(KEYSYM, "key.keyboard.keypad.5", GLFW.GLFW_KEY_KP_5);
			mapKey(KEYSYM, "key.keyboard.keypad.6", GLFW.GLFW_KEY_KP_6);
			mapKey(KEYSYM, "key.keyboard.keypad.7", GLFW.GLFW_KEY_KP_7);
			mapKey(KEYSYM, "key.keyboard.keypad.8", GLFW.GLFW_KEY_KP_8);
			mapKey(KEYSYM, "key.keyboard.keypad.9", GLFW.GLFW_KEY_KP_9);
			mapKey(KEYSYM, "key.keyboard.keypad.add", GLFW.GLFW_KEY_KP_ADD);
			mapKey(KEYSYM, "key.keyboard.keypad.decimal", GLFW.GLFW_KEY_KP_DECIMAL);
			mapKey(KEYSYM, "key.keyboard.keypad.enter", GLFW.GLFW_KEY_KP_ENTER);
			mapKey(KEYSYM, "key.keyboard.keypad.equal", GLFW.GLFW_KEY_KP_EQUAL);
			mapKey(KEYSYM, "key.keyboard.keypad.multiply", GLFW.GLFW_KEY_KP_MULTIPLY);
			mapKey(KEYSYM, "key.keyboard.keypad.divide", GLFW.GLFW_KEY_KP_DIVIDE);
			mapKey(KEYSYM, "key.keyboard.keypad.subtract", GLFW.GLFW_KEY_KP_SUBTRACT);
			mapKey(KEYSYM, "key.keyboard.down", GLFW.GLFW_KEY_DOWN);
			mapKey(KEYSYM, "key.keyboard.left", GLFW.GLFW_KEY_LEFT);
			mapKey(KEYSYM, "key.keyboard.right", GLFW.GLFW_KEY_RIGHT);
			mapKey(KEYSYM, "key.keyboard.up", GLFW.GLFW_KEY_UP);
			mapKey(KEYSYM, "key.keyboard.apostrophe", GLFW.GLFW_KEY_APOSTROPHE);
			mapKey(KEYSYM, "key.keyboard.backslash", GLFW.GLFW_KEY_BACKSLASH);
			mapKey(KEYSYM, "key.keyboard.comma", GLFW.GLFW_KEY_COMMA);
			mapKey(KEYSYM, "key.keyboard.equal", GLFW.GLFW_KEY_EQUAL);
			mapKey(KEYSYM, "key.keyboard.grave.accent", GLFW.GLFW_KEY_GRAVE_ACCENT);
			mapKey(KEYSYM, "key.keyboard.left.bracket", GLFW.GLFW_KEY_LEFT_BRACKET);
			mapKey(KEYSYM, "key.keyboard.minus", GLFW.GLFW_KEY_MINUS);
			mapKey(KEYSYM, "key.keyboard.period", GLFW.GLFW_KEY_PERIOD);
			mapKey(KEYSYM, "key.keyboard.right.bracket", GLFW.GLFW_KEY_RIGHT_BRACKET);
			mapKey(KEYSYM, "key.keyboard.semicolon", GLFW.GLFW_KEY_SEMICOLON);
			mapKey(KEYSYM, "key.keyboard.slash", GLFW.GLFW_KEY_SLASH);
			mapKey(KEYSYM, "key.keyboard.space", GLFW.GLFW_KEY_SPACE);
			mapKey(KEYSYM, "key.keyboard.tab", GLFW.GLFW_KEY_TAB);
			mapKey(KEYSYM, "key.keyboard.left.alt", GLFW.GLFW_KEY_LEFT_ALT);
			mapKey(KEYSYM, "key.keyboard.left.control", GLFW.GLFW_KEY_LEFT_CONTROL);
			mapKey(KEYSYM, "key.keyboard.left.shift", GLFW.GLFW_KEY_LEFT_SHIFT);
			mapKey(KEYSYM, "key.keyboard.left.win", GLFW.GLFW_KEY_LEFT_SUPER);
			mapKey(KEYSYM, "key.keyboard.right.alt", GLFW.GLFW_KEY_RIGHT_ALT);
			mapKey(KEYSYM, "key.keyboard.right.control", GLFW.GLFW_KEY_RIGHT_CONTROL);
			mapKey(KEYSYM, "key.keyboard.right.shift", GLFW.GLFW_KEY_RIGHT_SHIFT);
			mapKey(KEYSYM, "key.keyboard.right.win", GLFW.GLFW_KEY_RIGHT_SUPER);
			mapKey(KEYSYM, "key.keyboard.enter", GLFW.GLFW_KEY_ENTER);
			mapKey(KEYSYM, "key.keyboard.escape", GLFW.GLFW_KEY_ESCAPE);
			mapKey(KEYSYM, "key.keyboard.backspace", GLFW.GLFW_KEY_BACKSPACE);
			mapKey(KEYSYM, "key.keyboard.delete", GLFW.GLFW_KEY_DELETE);
			mapKey(KEYSYM, "key.keyboard.end", GLFW.GLFW_KEY_END);
			mapKey(KEYSYM, "key.keyboard.home", GLFW.GLFW_KEY_HOME);
			mapKey(KEYSYM, "key.keyboard.insert", GLFW.GLFW_KEY_INSERT);
			mapKey(KEYSYM, "key.keyboard.page.down", GLFW.GLFW_KEY_PAGE_DOWN);
			mapKey(KEYSYM, "key.keyboard.page.up", GLFW.GLFW_KEY_PAGE_UP);
			mapKey(KEYSYM, "key.keyboard.caps.lock", GLFW.GLFW_KEY_CAPS_LOCK);
			mapKey(KEYSYM, "key.keyboard.pause", GLFW.GLFW_KEY_PAUSE);
			mapKey(KEYSYM, "key.keyboard.scroll.lock", GLFW.GLFW_KEY_SCROLL_LOCK);
			mapKey(KEYSYM, "key.keyboard.menu", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.print.screen", GLFW.GLFW_KEY_PRINT_SCREEN);
			mapKey(KEYSYM, "key.keyboard.world.1", GLFW.GLFW_KEY_UNKNOWN);
			mapKey(KEYSYM, "key.keyboard.world.2", GLFW.GLFW_KEY_UNKNOWN);
		}
	}
}
