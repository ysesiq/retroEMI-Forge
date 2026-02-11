package dev.emi.emi.api.stack;

import net.minecraft.nbt.NBTTagCompound;
import shim.net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.ApiStatus;

/**
 * Provides EMI context for a {@link TagKey} to construct stacks from the objects in the registry.
 * This allows EMI to construct tag ingredients from stacks from the given registry.
 */
@ApiStatus.Experimental
public interface EmiRegistryAdapter<T> {

    /**
     * @return The base class for objects in the registry.
     */
    Class<T> getBaseClass();

    /**
     * @return
     */
    TagKey.Type getRegistry();

    /**
     * Constructs an {@link EmiStack} from a given object from the registry, or {@link EmiStack#EMPTY} if somehow invalid.
     */
    EmiStack of(T t, NBTTagCompound componentChanges, long amount);

    /**
     * Convenience method for creating an {@link EmiRegistryAdapter}.
     */
    public static <T> EmiRegistryAdapter<T> simple(Class<T> clazz, TagKey.Type registry, StackConstructor<T> constructor) {
        return new EmiRegistryAdapter<T>() {

            @Override
            public Class<T> getBaseClass() {
                return clazz;
            }

            @Override
            public TagKey.Type getRegistry() {
                return registry;
            }

            @Override
            public EmiStack of(T t, NBTTagCompound componentChanges, long amount) {
                return constructor.of(t, componentChanges, amount);
            }
        };
    }

    public static interface StackConstructor<T> {
        EmiStack of(T t, NBTTagCompound componentChanges, long amount);
    }
}
