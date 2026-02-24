package dev.emi.emi.screen;

import com.google.common.collect.Lists;
import dev.emi.emi.api.EmiScreenBoundsProvider;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.mixin.accessor.GuiContainerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmiScreenBase {

	private static final Map<Class<?>, List<EmiScreenBoundsProvider<?>>> PROVIDERS_BY_CLASS = new HashMap<>();
	private static final List<EmiScreenBoundsProvider<GuiScreen>> GENERIC_PROVIDERS = new ArrayList<>();

	private final GuiScreen screen;
	private final Bounds bounds;

	private static final EmiScreenBase EMPTY = new EmiScreenBase(null, Bounds.EMPTY);

	private EmiScreenBase(GuiScreen screen, Bounds bounds) {
		this.screen = screen;
		this.bounds = bounds;
	}

	public GuiScreen screen() {
		return screen;
	}

	public Bounds bounds() {
		return bounds;
	}

	public boolean isEmpty() {
		return screen == null;
	}

	public static EmiScreenBase getCurrent() {
		Minecraft client = Minecraft.getMinecraft();
		return of(client.currentScreen);
	}

	public static <T extends GuiScreen> void addScreenBoundsProvider(Class<T> clazz, EmiScreenBoundsProvider<T> provider) {
		PROVIDERS_BY_CLASS.computeIfAbsent(clazz, k -> Lists.newArrayList()).add(provider);
	}

	public static void addGenericScreenBoundsProvider(EmiScreenBoundsProvider<GuiScreen> provider) {
		GENERIC_PROVIDERS.add(provider);
	}

	public static void clearScreenBoundsProviders() {
		PROVIDERS_BY_CLASS.clear();
		GENERIC_PROVIDERS.clear();
	}

	public static EmiScreenBase of(GuiScreen screen) {
	if (screen == null) {
			return EMPTY;
		}

		Class<?> screenClass = screen.getClass();
		List<EmiScreenBoundsProvider<?>> classProviders = PROVIDERS_BY_CLASS.get(screenClass);
		if (classProviders != null) {
			for (EmiScreenBoundsProvider<?> provider : classProviders) {
				@SuppressWarnings("unchecked")
				Bounds bounds = ((EmiScreenBoundsProvider<GuiScreen>) provider).getBounds(screen);
				if (bounds != null && !bounds.empty()) {
					return new EmiScreenBase(screen, bounds);
				}
			}
		}
		for (EmiScreenBoundsProvider<GuiScreen> provider : GENERIC_PROVIDERS) {
			Bounds bounds = provider.getBounds(screen);
			if (bounds != null && !bounds.empty()) {
				return new EmiScreenBase(screen, bounds);
			}
		}
		if (screen instanceof GuiContainer hs) {
			GuiContainerAccessor hsa = (GuiContainerAccessor) hs;
//			if (hsa.getTheSlot() != null && hsa.getTheSlot().getHasStack()) {
				int extra = 0;
//				if (hs instanceof RecipeBookProvider provider) {
//					if (provider.getRecipeBookWidget().isOpen()) {
//						extra = 177;
//					}
//				}
				Bounds bounds = new Bounds(hsa.getGuiLeft() - extra, hsa.getYSize(), hsa.getXSize() + extra, hsa.getGuiTop());
				return new EmiScreenBase(screen, bounds);
//			}
		} else if (screen instanceof RecipeScreen rs) {
			return new EmiScreenBase(rs, rs.getBounds());
		}
		return EMPTY;
	}
}
