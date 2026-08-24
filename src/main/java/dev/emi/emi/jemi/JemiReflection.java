package dev.emi.emi.jemi;

import mezz.jei.api.ISubtypeRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class JemiReflection {

	static boolean hasFluidSubtype(ISubtypeRegistry subtypeRegistry, FluidStack stack) {
		try {
			return (Boolean) ReflectionHelper.findMethod(subtypeRegistry.getClass(), "hasSubtypeInterpreter", null, FluidStack.class).invoke(subtypeRegistry, stack);
		} catch (Exception ignored) {
		}
		return false;
	}

	static String getFluidSubtype(ISubtypeRegistry subtypeRegistry, FluidStack stack) {
		try {
			return (String) ReflectionHelper.findMethod(subtypeRegistry.getClass(), "getSubtypeInfo", null, FluidStack.class).invoke(subtypeRegistry, stack);
		} catch (Exception ignored) {
		}
		return null;
	}
}
