package shim.net.minecraft.client.renderer;

import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class GlStateManager {
	public static void disableAlpha() {
		glEnable(GL_ALPHA_TEST);
	}
	public static void enableAlpha() {
		glEnable(GL_ALPHA_TEST);
	}

	public static void enableRescaleNormal() {
		glEnable(GL_RESCALE_NORMAL);
	}

	public static void disableRescaleNormal() {
		glDisable(GL_RESCALE_NORMAL);
	}

	public static void enableTexture2D() {
		glEnable(GL_TEXTURE_2D);
	}

	public static void disableTexture2D() {
		glDisable(GL_TEXTURE_2D);
	}

	public static void viewport(int x, int y, int width, int height) {
		GL11.glViewport(x, y, width, height);
	}

	public static void ortho(double left, double right, double bottom, double top, double zNear, double zFar) {
		GL11.glOrtho(left, right, bottom, top, zNear, zFar);
	}

	public static void glReadPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
		GL11.glReadPixels(x, y, width, height, format, type, pixels);
	}

	public static void shadeModel(int mode) {
		GL11.glShadeModel(mode);
	}

	public static void colorLogicOp(int opcode) {
		GL11.glLogicOp(opcode);
	}
}
