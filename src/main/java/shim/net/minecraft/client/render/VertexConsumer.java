package shim.net.minecraft.client.render;

public interface VertexConsumer {
    VertexConsumer vertex(float x, float y, float z);
    VertexConsumer color(int r, int g, int b, int a);
    VertexConsumer texture(float u, float v);
    VertexConsumer overlay(int u, int v);
    VertexConsumer light(int u, int v);
    VertexConsumer normal(float x, float y, float z);
}
