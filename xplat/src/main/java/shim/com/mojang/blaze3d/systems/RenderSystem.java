package shim.com.mojang.blaze3d.systems;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;

public class RenderSystem {

	public static void enableDepthTest() {
		glEnable(GL_DEPTH_TEST);
	}

	public static void disableDepthTest() {
		glDisable(GL_DEPTH_TEST);
	}

	public static void enableScissor(int x, int y, int width, int height) {
		glEnable(GL_SCISSOR_TEST);
		glScissor(x, y, width, height);
	}

	public static void disableScissor() {
		glDisable(GL_SCISSOR_TEST);
	}

	public static void enableLighting() {
		GL11.glEnable(GL_LIGHTING);
	}

	public static void disableLighting() {
		glDisable(GL_LIGHTING);
	}

	public static void enableBlend() {
		glEnable(GL_BLEND);
	}

	public static void disableBlend() {
		glDisable(GL_BLEND);
	}

	public static void defaultBlendFunc() {
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public static void blendFunc(int srcFactorRGB, int dstFactorRGB) {
		glBlendFunc(srcFactorRGB, dstFactorRGB);
	}

	public static void blendFuncSeparate(int srcFactorRGB, int dstFactorRGB, int srcFactorAlpha, int dstFactorAlpha) {
		glBlendFuncSeparate(srcFactorRGB, dstFactorRGB, srcFactorAlpha, dstFactorAlpha);
	}

	public static MatrixStack getModelViewStack() {
		glMatrixMode(GL_MODELVIEW);
		return MatrixStack.INSTANCE;
	}

	public static void applyModelViewMatrix() {
		FloatBuffer currentMatrix = BufferUtils.createFloatBuffer(16);
		glGetFloat(GL_MODELVIEW_MATRIX, currentMatrix);
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.load(currentMatrix);
		glMatrixMode(GL_MODELVIEW);
		glLoadMatrix(matrixToFloatBuffer(matrix4f));
	}

	public static MatrixStack getProjectionMatrix() {
		glMatrixMode(GL_PROJECTION);
		return MatrixStack.INSTANCE;
	}

	public static void setProjectionMatrix(MatrixStack projection) {
		glMatrixMode(GL_PROJECTION);
		projection.pushMatrix();
		projection.identity();
	}

	public static void viewport(int x, int y, int width, int height) {
		glViewport(x, y, width, height);
	}

	public static FloatBuffer matrixToFloatBuffer(Matrix4f matrix) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		buffer.put(matrix.m00).put(matrix.m01).put(matrix.m02).put(matrix.m03);
		buffer.put(matrix.m10).put(matrix.m11).put(matrix.m12).put(matrix.m13);
		buffer.put(matrix.m20).put(matrix.m21).put(matrix.m22).put(matrix.m23);
		buffer.put(matrix.m30).put(matrix.m31).put(matrix.m32).put(matrix.m33);
		buffer.flip();
		return buffer;
	}

	public static void setShaderColor(float r, float g, float b, float a) {
		glColor4f(r, g, b, a);
	}

	public static void setShaderTexture(int i, ResourceLocation id) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(id);
//		glBindTexture(GL_TEXTURE_2D + i, Minecraft.getMinecraft().getTextureManager().getTexture(id).getGlTextureId());
	}

	public static void colorMask(boolean r, boolean g, boolean b, boolean a) {
		glColorMask(r, g, b, a);
		glDepthMask(a);
	}

}
