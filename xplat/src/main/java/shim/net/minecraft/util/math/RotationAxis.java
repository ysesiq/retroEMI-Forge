package shim.net.minecraft.util.math;

import static org.lwjgl.opengl.GL11.glRotatef;

public class RotationAxis {

	public static final RotationAxis POSITIVE_X = new RotationAxis(1, 0, 0);
	public static final RotationAxis POSITIVE_Y = new RotationAxis(0, 1, 0);
	public static final RotationAxis POSITIVE_Z = new RotationAxis(0, 0, 1);

	public static final RotationAxis NEGATIVE_X = new RotationAxis(-1,  0,  0);
	public static final RotationAxis NEGATIVE_Y = new RotationAxis( 0, -1,  0);
	public static final RotationAxis NEGATIVE_Z = new RotationAxis( 0,  0, -1);

	private final float x, y, z;

	public RotationAxis(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Runnable rotationDegrees(float deg) {
		return () -> glRotatef(deg, x, y, z);
	}

}
