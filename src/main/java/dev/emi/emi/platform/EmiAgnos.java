package dev.emi.emi.platform;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.registry.EmiPluginContainer;
import shim.net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.nbt.NBTTagCompound;
import shim.net.minecraft.registry.tag.ItemKey;
import shim.net.minecraft.text.Text;
import net.minecraftforge.fluids.Fluid;

public abstract class EmiAgnos {
	public static EmiAgnos delegate;

	static {
		try {
			Class.forName("dev.emi.emi.platform.fabric.EmiAgnosFabric");
		} catch (Throwable t) {
		}
		try {
			Class.forName("dev.emi.emi.platform.forge.EmiAgnosForge");
		} catch (Throwable t) {
		}
		try {
			Class.forName("dev.emi.emi.platform.nil.EmiAgnosNil");
		} catch (Throwable t) {
		}
	}

	public static boolean isForge() {
		return delegate.isForgeAgnos();
	}

	protected abstract boolean isForgeAgnos();

	public static String getModName(String namespace) {
		return delegate.getModNameAgnos(namespace);
	}

	protected abstract String getModNameAgnos(String namespace);

	public static Path getConfigDirectory() {
		return delegate.getConfigDirectoryAgnos();
	}

	protected abstract Path getConfigDirectoryAgnos();

	public static boolean isDevelopmentEnvironment() {
		return delegate.isDevelopmentEnvironmentAgnos();
	}

	protected abstract boolean isDevelopmentEnvironmentAgnos();

	public static boolean isModLoaded(String id) {
		return delegate.isModLoadedAgnos(id);
	}

	protected abstract boolean isModLoadedAgnos(String id);

	public static List<String> getAllModNames(String id) {
		return delegate.getAllModNamesAgnos(id);
	}

	protected abstract List<String> getAllModNamesAgnos(String id);

	public static List<String> getAllModAuthors(String id) {
		return delegate.getAllModAuthorsAgnos(id);
	}

	protected abstract List<String> getAllModAuthorsAgnos(String id);

	public static List<String> getModsWithPlugins() {
		return delegate.getModsWithPluginsAgnos();
	}

	protected abstract List<String> getModsWithPluginsAgnos();

	public static List<EmiPluginContainer> getPlugins() {
		return delegate.getPluginsAgnos();
	}

	protected abstract List<EmiPluginContainer> getPluginsAgnos();

	public static void addBrewingRecipes(EmiRegistry registry) {
		delegate.addBrewingRecipesAgnos(registry);
	}

	protected abstract void addBrewingRecipesAgnos(EmiRegistry registry);

	public static List<TooltipComponent> getItemTooltip(ItemStack stack) {
		return delegate.getItemTooltipAgnos(stack);
	}

	protected abstract List<TooltipComponent> getItemTooltipAgnos(ItemStack stack);

	public static Text getFluidName(Fluid fluid, NBTTagCompound componentChanges) {
		return delegate.getFluidNameAgnos(fluid, componentChanges);
	}

	protected abstract Text getFluidNameAgnos(Fluid fluid, NBTTagCompound componentChanges);

	public static List<Text> getFluidTooltip(Fluid fluid, NBTTagCompound componentChanges) {
		return delegate.getFluidTooltipAgnos(fluid, componentChanges);
	}

	protected abstract List<Text> getFluidTooltipAgnos(Fluid fluid, NBTTagCompound componentChanges);

	public static boolean isFloatyFluid(FluidEmiStack stack) {
		return delegate.isFloatyFluidAgnos(stack);
	}

	protected abstract boolean isFloatyFluidAgnos(FluidEmiStack stack);

	public static void renderFluid(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta) {
		renderFluid(stack, matrices, x, y, delta, 0, 0, 16, 16);
	}

	public static void renderFluid(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		delegate.renderFluidAgnos(stack, matrices, x, y, delta, xOff, yOff, width, height);
	}

	protected abstract void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height);

	public static EmiStack createFluidStack(Object object) {
		return delegate.createFluidStackAgnos(object);
	}

	protected abstract EmiStack createFluidStackAgnos(Object object);

	public static boolean canBatch(ItemStack stack) {
		return delegate.canBatchAgnos(stack);
	}

	protected abstract boolean canBatchAgnos(ItemStack stack);

	public static Map<ItemKey, Integer> getFuelMap() {
		return delegate.getFuelMapAgnos();
	}

	protected abstract Map<ItemKey, Integer> getFuelMapAgnos();

//	public static BakedModel getBakedTagModel(ResourceLocation id) {
//		return delegate.getBakedTagModelAgnos(id);
//	}
//
//	protected abstract BakedModel getBakedTagModelAgnos(ResourceLocation id);

	public static boolean isEnchantable(ItemStack stack, Enchantment enchantment) {
		return delegate.isEnchantableAgnos(stack, enchantment);
	}

	protected abstract boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment);
}
