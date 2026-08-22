package dev.emi.emi.jemi;

import net.minecraftforge.fluids.FluidStack;

public interface IJeiSubtypeRegistry {
	default boolean hasSubtypeInterpreter(FluidStack fluidStack) {
		return false;
	}

	default String getSubtypeInfo(FluidStack fluidStack) {
		return null;
	}

	@FunctionalInterface
	interface FluidSubtypeInterpreter {
		String apply(FluidStack fluidStack);
	}
}
