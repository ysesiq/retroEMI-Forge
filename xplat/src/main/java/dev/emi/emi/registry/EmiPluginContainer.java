package dev.emi.emi.registry;

import com.github.bsideup.jabel.Desugar;
import dev.emi.emi.api.EmiPlugin;

@Desugar
public record EmiPluginContainer(EmiPlugin plugin, String id) {
}
