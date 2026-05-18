package shim.net.minecraft.client.render;

public interface VertexConsumerProvider {
    VertexConsumer getBuffer(RenderLayer layer);
}
