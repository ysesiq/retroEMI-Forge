package dev.emi.emi.nemi;

import codechicken.nei.LayoutManager;
import codechicken.nei.LayoutStyleMinecraft;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.Minecraft;
import java.lang.reflect.Method;

public class NemiPlugin implements EmiPlugin {
	public static boolean isNEILoaded = false;
	private static final Minecraft client = Minecraft.getMinecraft();

	public static void onLoad() {
		try {
			Class<?> apiClass = Class.forName("codechicken.nei.api.API");
			Method registerMethod = apiClass.getMethod("registerNEIGuiHandler",
				Class.forName("codechicken.nei.api.INEIGuiHandler"));
			Object handler = new NemiScreenHandler();
			registerMethod.invoke(null, handler);
		} catch (Exception e) {
			EmiLog.error("Failed to register NEI GUI handler via reflection", e);
		}
        isNEILoaded = true;
	}

	@Override
	public void register(EmiRegistry registry) {
		registry.addGenericExclusionArea((screen, consumer) -> {
			final LayoutStyleMinecraft layout = (LayoutStyleMinecraft) LayoutManager.getLayoutStyle();
			if (layout != null && !(client.currentScreen instanceof RecipeScreen)) {
				final int rows = (int) Math.ceil((double) layout.buttonCount / layout.numButtons);
				final int diff = rows * 19 + 2;
				consumer.accept(new Bounds(0, 0, layout.numButtons * 19, diff));
			}
		});
	}
}
