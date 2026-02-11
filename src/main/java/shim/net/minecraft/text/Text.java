package shim.net.minecraft.text;

import com.rewindmc.retroemi.RetroEMI;

public interface Text {

	String asString();
	OrderedText asOrderedText();
	MutableText copy();

	default String getString() {
		return asString();
	}


	static MutableText empty() {
		return new TextImpl(() -> "", "");
	}

	static MutableText literal(String s) {
		return new TextImpl(() -> s, "");
	}

	static MutableText translatable(String s) {
		return new TextImpl(() -> RetroEMI.translate(s), "");
	}

	static MutableText translatable(String s, Object... arg) {
		return new TextImpl(() -> RetroEMI.translate(s, arg), "");
	}

}
