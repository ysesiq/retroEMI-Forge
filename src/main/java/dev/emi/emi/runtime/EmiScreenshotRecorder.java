package dev.emi.emi.runtime;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import javax.imageio.ImageIO;


import shim.com.mojang.blaze3d.systems.RenderSystem;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import dev.emi.emi.EmiPort;
import dev.emi.emi.config.EmiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import shim.net.minecraft.client.util.math.MatrixStack;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

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

		Framebuffer framebuffer = new Framebuffer(width * scale, height * scale, true);
		framebuffer.setFramebufferColor(0f, 0f, 0f, 0f);
		if (Minecraft.isRunningOnMac) framebuffer.framebufferClear();

		framebuffer.bindFramebuffer(true);

		MatrixStack backupProj = RenderSystem.getProjectionMatrix();
		RenderSystem.setProjectionMatrix(new MatrixStack());

		GL11.glOrtho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
		MatrixStack view = RenderSystem.getModelViewStack();
		RenderSystem.getModelViewStack();
		view.pushMatrix();
		view.identity();
		view.translate(0.0f, 0.0f, -2000.0f);
		RenderSystem.viewport(0, 0, framebuffer.framebufferWidth, framebuffer.framebufferHeight);
		EmiPort.applyModelViewMatrix();


		renderer.run();

		RenderSystem.setProjectionMatrix(backupProj);
		view.popMatrix();
		EmiPort.applyModelViewMatrix();

		framebuffer.unbindFramebuffer();
		client.getFramebuffer().bindFramebuffer(true);

		saveScreenshotInner(client.mcDataDir, path, framebuffer,
			message -> client.ingameGUI.getChatGUI().printChatMessage(message));
	}

	private static void saveScreenshotInner(File gameDirectory, String suggestedPath, Framebuffer framebuffer, Consumer<IChatComponent> messageReceiver) {
		BufferedImage nativeImage = takeScreenshot(framebuffer);

		File screenshots = new File(gameDirectory, SCREENSHOTS_DIRNAME);
		screenshots.mkdir();

		String filename = getScreenshotFilename(screenshots, suggestedPath);
		File file = new File(screenshots, filename);

		// Make sure the parent file exists. Note: `/`s in suggestedPath are valid, as they indicate subdirectories. Java even translates this
		// correctly on Windows.
		File parent = file.getParentFile();
		parent.mkdirs();

		try {
			ImageIO.write(nativeImage, "png", file);

			IChatComponent text = new ChatComponentText(filename)
				.setChatStyle(new ChatStyle().setUnderlined(true).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
			messageReceiver.accept(new ChatComponentTranslation("screenshot.success", text));
		} catch (Throwable e) {
			EmiLog.error("Failed to write screenshot", e);
			messageReceiver.accept(new ChatComponentTranslation("screenshot.failure", e.getMessage()));
		}
	}

	private static BufferedImage takeScreenshot(Framebuffer framebuffer) {
		framebuffer.bindFramebuffer(false);
		BufferedImage image = new BufferedImage(framebuffer.framebufferWidth, framebuffer.framebufferHeight, BufferedImage.TYPE_INT_ARGB);
		ByteBuffer buffer = BufferUtils.createByteBuffer(framebuffer.framebufferWidth * framebuffer.framebufferHeight * 4);

		GL11.glReadPixels(0, 0, framebuffer.framebufferWidth, framebuffer.framebufferHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int y = 0; y < framebuffer.framebufferHeight; y++) {
			for (int x = 0; x < framebuffer.framebufferWidth; x++) {
				int i = (y * framebuffer.framebufferWidth + x) * 4;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				int a = buffer.get(i + 3) & 0xFF;
				data[(framebuffer.framebufferHeight - 1 - y) * framebuffer.framebufferWidth + x] = (a << 24) | (r << 16) | (g << 8) | b;
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
