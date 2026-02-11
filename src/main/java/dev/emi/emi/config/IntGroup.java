package dev.emi.emi.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.emi.emi.EmiPort;
import shim.net.minecraft.text.Text;

public class IntGroup {
	public final String baseTranslation;
	public final int size;
	public final List<String> names;
	public final List<Integer> values;

	public IntGroup(String baseTranslation, List<String> names, List<Integer> values) {
		this.baseTranslation = baseTranslation;
		this.size = names.size();
		this.names = names;
		this.values = new ArrayList<>();
		this.values.addAll(values);
	}

	public Text getValueTranslation(int i) {
		return EmiPort.translatable(baseTranslation + names.get(i));
	}

	public String serialize() {
		return values.stream().map(i -> "" + i).collect(Collectors.joining(", "));
	}

	public void deserialize(String text) {
		String[] parts = text.split(",");
		if (parts.length == size) {
			for (int i = 0; i < size; i++) {
				values.set(i, Integer.parseInt(parts[i].trim()));
			}
		}
	}
}
