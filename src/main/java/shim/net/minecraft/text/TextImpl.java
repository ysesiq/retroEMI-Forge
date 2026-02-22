package shim.net.minecraft.text;

import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TextImpl implements OrderedText, Text, MutableText {

	private final Supplier<String> text;
	private String style;
	private final List<TextImpl> siblings = new ArrayList<>();

	public TextImpl(Supplier<String> text, String style) {
		this.text = text;
		this.style = style;
	}

	private void appendTo(StringBuilder sb) {
		sb.append(style);
		sb.append(text.get());
		for (var sibling : siblings) {
			sibling.appendTo(sb);
		}
	}

	@Override
	public String asString() {
		var sb = new StringBuilder();
		appendTo(sb);
		if (!style.isEmpty()) {
			sb.append("§r");
		}
		return sb.toString();
	}

	@Override @Deprecated
	public String toString() {
		// Will get called by vanilla formatting
		return asString();
	}

	@Override
	public MutableText setStyle(Style s) {
		this.style = s.toString();
		return this;
	}

	@Override
	public MutableText formatted(EnumChatFormatting fmt) {
		this.style += fmt;
		return this;
	}

	@Override
	public MutableText formatted(EnumChatFormatting... fmt) {
		StringBuilder sb = new StringBuilder(style.length()+(fmt.length*2));
		sb.append(style);
		for (var f : fmt) sb.append(f);
		style = sb.toString();
		return this;
	}

	@Override
	public MutableText append(Text text) {
		this.siblings.add((TextImpl)text);
		return this;
	}

	@Override
	public OrderedText asOrderedText() {
		return this;
	}

	@Override
	public MutableText copy() {
		var i = new TextImpl(text, style);
		i.siblings.addAll(siblings);
		return i;
	}

}
