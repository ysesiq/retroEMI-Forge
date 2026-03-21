package shim.com.mojang.blaze3d.systems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import shim.net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class RenderSystem {

    public static void enableDepthTest() {
        GlStateManager.enableDepth();
	}

	public static void disableDepthTest() {
        GlStateManager.disableDepth();
	}

	public static void enableScissor(int x, int y, int width, int height) {
		glEnable(GL_SCISSOR_TEST);
		glScissor(x, y, width, height);
	}

	public static void disableScissor() {
		glDisable(GL_SCISSOR_TEST);
	}

	public static void enableLighting() {
        GlStateManager.enableLighting();
	}

	public static void disableLighting() {
        GlStateManager.disableLighting();
	}

	public static void enableBlend() {
        GlStateManager.enableBlend();
	}

	public static void disableBlend() {
        GlStateManager.disableBlend();
	}

	public static void defaultBlendFunc() {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	}

	public static void blendFunc(int sfactor, int dfactor) {
        GlStateManager.blendFunc(sfactor, dfactor);
	}

	public static MatrixStack getModelViewStack() {
        GlStateManager.matrixMode(GL_MODELVIEW);
		return MatrixStack.INSTANCE;
	}

	public static void applyModelViewMatrix() {
		FloatBuffer currentMatrix = BufferUtils.createFloatBuffer(16);
        GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, currentMatrix);
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.load(currentMatrix);
        GlStateManager.matrixMode(GL_MODELVIEW);
		GL11.glLoadMatrix(matrixToFloatBuffer(matrix4f));
	}

    public static MatrixStack getProjectionMatrix() {
        GlStateManager.matrixMode(GL_PROJECTION);
        return MatrixStack.INSTANCE;
    }

    public static void setProjectionMatrix(MatrixStack projection) {
        GlStateManager.matrixMode(GL_PROJECTION);
        projection.pushMatrix();
        projection.identity();
    }

    public static void viewport(int x, int y, int width, int height) {
        GlStateManager.viewport(x, y, width, height);
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
        GlStateManager.color(r, g, b, a);
	}

	public static void setShaderTexture(int i, ResourceLocation id) {
        GlStateManager.bindTexture(i);
        Minecraft.getMinecraft().getTextureManager().bindTexture(id);
//		glBindTexture(GL_TEXTURE_2D + i, Minecraft.getMinecraft().getTextureManager().getTexture(id).getGlTextureId());
	}

	public static void colorMask(boolean r, boolean g, boolean b, boolean a) {
        GlStateManager.colorMask(r, g, b, a);
        GlStateManager.depthMask(a);
	}

}
