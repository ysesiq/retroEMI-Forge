package dev.emi.emi.api.stack;

import org.jetbrains.annotations.ApiStatus;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents a type that can be implicitly converted into an EmiStack.
 */
@ApiStatus.Experimental
public interface EmiStackConvertible {

	/**
	 * @return The default representation of this type as an EmiStack.
	 * If the type has an amount of its own, it will be used.
	 * Otherwise, it'll use the default amount for the stack type.
	 */
	@SideOnly(Side.CLIENT)
	@ApiStatus.Experimental
	default EmiStack emi() {
		throw new IllegalStateException();
	}

	/**
	 * @return The default representation of this type as an EmiStack.
	 * Uses the provided amount.
	 */
    @SideOnly(Side.CLIENT)
	@ApiStatus.Experimental
	default EmiStack emi(long amount) {
		throw new IllegalStateException();
	}
}
