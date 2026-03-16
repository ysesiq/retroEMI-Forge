package shim.net.minecraft.client.util.math;

import net.minecraft.client.renderer.GlStateManager;

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
}
