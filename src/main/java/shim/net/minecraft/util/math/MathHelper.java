package shim.net.minecraft.util.math;

public class MathHelper {
    public static int lerp(float delta, int start, int end) {
        return start + net.minecraft.util.math.MathHelper.floor(delta * (float)(end - start));
    }
}
