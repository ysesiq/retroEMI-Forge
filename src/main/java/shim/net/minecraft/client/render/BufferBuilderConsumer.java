package shim.net.minecraft.client.render;

import net.minecraft.client.renderer.BufferBuilder;

public record BufferBuilderConsumer(BufferBuilder builder) implements VertexConsumer {

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        builder.pos(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        builder.color(r, g, b, a);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        builder.tex(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
//        builder.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        builder.lightmap(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        builder.normal(x, y, z);
        return this;
    }
}
