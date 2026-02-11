package shim.com.mojang.blaze3d.systems;

import net.minecraft.client.Minecraft;
import shim.net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class RenderSystem {

    public static void enableDepthTest() {
        glEnable(GL_DEPTH_TEST);
    }

    public static void disableDepthTest() {
        glDisable(GL_DEPTH_TEST);
    }

    public static void enableLighting() {
        glEnable(GL_LIGHTING);
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

    public static void blendFunc(int sfactor, int dfactor) {
        glBlendFunc(sfactor, dfactor);
    }

    public static MatrixStack getModelViewStack() {
        return MatrixStack.INSTANCE;
    }

    public static void applyModelViewMatrix() {
        FloatBuffer currentMatrix = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, currentMatrix);
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(currentMatrix);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(matrixToFloatBuffer(matrix4f));
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
        glBindTexture(GL_TEXTURE_2D + i, Minecraft.getMinecraft().renderEngine.getTexture(id).getGlTextureId());
    }

    public static void colorMask(boolean r, boolean g, boolean b, boolean a) {
        glColorMask(r, g, b, a);
        glDepthMask(a);
    }

}
