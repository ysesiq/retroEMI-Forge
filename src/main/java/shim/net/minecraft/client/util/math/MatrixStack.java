package shim.net.minecraft.client.util.math;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;

public class MatrixStack {

	public static final MatrixStack INSTANCE = new MatrixStack();

	public MatrixStack() {}

	public void pushMatrix() {
		GlStateManager.pushMatrix();
	}

	public void popMatrix() {
		GlStateManager.popMatrix();
	}

	public void translate(double x, double y, double z) {
		GlStateManager.translate(x, y, z);
	}

	public void scale(double x, double y, double z) {
		GlStateManager.scale(x, y, z);
	}

	public void multiply(Runnable r) {
		r.run();
	}

	public void push() {
		GlStateManager.pushMatrix();
	}

	public void pop() {
		GlStateManager.popMatrix();
	}

	public void identity() {
		GlStateManager.loadIdentity();
	}

	public void multiplyPositionMatrix(Matrix4f matrix) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        buffer.put(matrix.m00()).put(matrix.m01()).put(matrix.m02()).put(matrix.m03());
        buffer.put(matrix.m10()).put(matrix.m11()).put(matrix.m12()).put(matrix.m13());
        buffer.put(matrix.m20()).put(matrix.m21()).put(matrix.m22()).put(matrix.m23());
        buffer.put(matrix.m30()).put(matrix.m31()).put(matrix.m32()).put(matrix.m33());
        buffer.flip();
        GL11.glMultMatrix(buffer);
    }
}
