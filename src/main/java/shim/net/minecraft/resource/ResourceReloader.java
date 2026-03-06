package shim.net.minecraft.resource;

public interface ResourceReloader {
	void reload();

	default String getName() {
		return this.getClass().getSimpleName();
	}
}
