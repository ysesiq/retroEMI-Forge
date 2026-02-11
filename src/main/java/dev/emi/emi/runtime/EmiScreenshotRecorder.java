package dev.emi.emi.runtime;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.*;
import net.minecraft.util.Utils;

public class EmiScreenshotRecorder {
	private static final String SCREENSHOTS_DIRNAME = "screenshots";

	/**
	 * Saves a screenshot to the game's `screenshots` directory, doing the appropriate setup so that anything rendered in renderer will be captured
	 * and saved.
	 * <p>
	 * <b>Note:</b> the path can have <code>/</code> characters, indicating subdirectories. Java handles these correctly on Windows. The path should
	 * <b>not</b> contain the <code>.png</code> extension, as that will be added after checking for duplicates. If a file with this path already
	 * exists, then path will be suffixed with a <code>_#</code>, before adding the <code>.png</code> extension, where <code>#</code> represents an
	 * increasing number to avoid conflicts.
	 * <p>
	 * <b>Note 2:</b> The width and height parameters are reflected in the viewport when rendering. But the EMI-config
	 * <code>ui.recipe-screenshot-scale</code> value causes the resulting image to be scaled.
	 *
	 * @param path     the path to save the screenshot to, without extension.
	 * @param width    the width of the screenshot, not counting EMI-config scale.
	 * @param height   the height of the screenshot, not counting EMI-config scale.
	 * @param renderer a function to render the things being screenshotted.
	 */
	public static void saveScreenshot(String path, int width, int height, Runnable renderer) {
		saveScreenshotInner(path, width, height, renderer);
	}

	private static void saveScreenshotInner(String path, int width, int height, Runnable renderer) {
		Minecraft client = Minecraft.getMinecraft();

		int scale;
		if (EmiConfig.recipeScreenshotScale < 1) {
			scale = EmiPort.getGuiScale(client);
		} else {
			scale = EmiConfig.recipeScreenshotScale;
		}

		GL11.glColor4f(0f, 0f, 0f, 0f);
		GL11.glPushAttrib(GL11.GL_VIEWPORT_BIT | GL11.GL_TRANSFORM_BIT);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
		GL11.glViewport(0, 0, width * scale, height * scale);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		renderer.run();

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glPopAttrib();
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		BufferedImage framebuffer = takeScreenshot(width * scale, height * scale);

		saveScreenshotInner(client.mcDataDir, path, framebuffer,
			message -> client.ingameGUI.getChatGUI().printChatMessage(message));
	}

	private static void saveScreenshotInner(File gameDirectory, String suggestedPath, BufferedImage framebuffer, Consumer<IChatComponent> messageReceiver) {
		File screenshots = new File(gameDirectory, SCREENSHOTS_DIRNAME);
		screenshots.mkdir();

		String filename = getScreenshotFilename(screenshots, suggestedPath);
		File file = new File(screenshots, filename);

		// Make sure the parent file exists. Note: `/`s in suggestedPath are valid, as they indicate subdirectories. Java even translates this
		// correctly on Windows.
		File parent = file.getParentFile();
		parent.mkdirs();

		Utils.getIoWorkerExecutor().execute(() -> {
			try {
				ImageIO.write(framebuffer, "png", file);

				IChatComponent text = new ChatComponentText(filename)
				    .setChatStyle(new ChatStyle().setUnderlined(true).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
				messageReceiver.accept(new ChatComponentTranslation("screenshot.success", text));
			} catch (Throwable e) {
				EmiLog.error("Failed to write screenshot", e);
				messageReceiver.accept(new ChatComponentTranslation("screenshot.failure", e.getMessage()));
			}
		});
	}

	private static BufferedImage takeScreenshot(int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		int[] data = ((java.awt.image.DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int y = 0; y < height; y++) {
		    for (int x = 0; x < width; x++) {
				int i = (y * width + x) * 4;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				int a = buffer.get(i + 3) & 0xFF;
				data[(height - 1 - y) * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
		    }
		}

		return image;
	}

	private static String getScreenshotFilename(File directory, String path) {
		int i = 1;
		while ((new File(directory, path + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
			++i;
		}
		return path + (i == 1 ? "" : "_" + i) + ".png";
	}
}
