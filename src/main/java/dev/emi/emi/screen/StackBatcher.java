package dev.emi.emi.screen;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.renderer.vertex.VertexBuffer;
import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;
import shim.com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.Minecraft;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.render.BufferBuilderConsumer;
import shim.net.minecraft.client.render.RenderLayer;
import shim.net.minecraft.client.render.VertexConsumer;
import shim.net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;

/**
 * @author Una "unascribed" Thompson
 */
public class StackBatcher {
//	private static MethodHandle sodiumSpriteHandle;
//
//	static {
//		try {
//			Class<?> clazz = null;
//			try {
//				// Try Sodium 0.5 name
//				clazz = Class.forName("me.jellysquid.mods.sodium.client.render.texture.SpriteUtil");
//			} catch (Throwable t) {
//			}
//			sodiumSpriteHandle = MethodHandles.lookup()
//				.findStatic(clazz, "markSpriteActive", MethodType.methodType(void.class, Sprite.class));
//			if (sodiumSpriteHandle != null) {
//				EmiLog.info("Discovered Sodium");
//			}
//		} catch (Throwable e) {
//		}
//	}

	public interface Batchable {
		boolean isSideLit();
		boolean isUnbatchable();
		void setUnbatchable();
		void renderForBatch(VertexConsumerProvider vcp, DrawContext draw, int x, int y, int z, float delta);
	}

	private final BatcherVertexConsumerProvider imm;
	private final VertexConsumerProvider unlitFacade;
	private final Map<RenderLayer, ByteBuffer> buffers = new LinkedHashMap<>();
	private final Set<Object> spritesToUpdate = Sets.newHashSet();
	private boolean populated = false;
	private boolean dirty = false;
	private int x;
	private int y;
	private int z;

//	public static final List<RenderLayer> EXTRA_RENDER_LAYERS = Lists.newArrayList();

	public static boolean isEnabled() {
		return EmiConfig.useBatchedRenderer;
	}

	public StackBatcher() {
//		Map<RenderLayer, BufferAllocator> buffers = new HashMap<>();
//		assign(buffers, RenderLayer.getSolid());
//		assign(buffers, RenderLayer.getCutout());
//		assign(buffers, RenderLayer.getTranslucent());
//		assign(buffers, TexturedRenderLayers.getEntitySolid());
//		assign(buffers, TexturedRenderLayers.getEntityCutout());
//		assign(buffers, TexturedRenderLayers.getEntityTranslucentCull());
//		assign(buffers, RenderLayer.getGlint());
//		//assign(buffers, RenderLayer.getDirectGlint());
//		assign(buffers, RenderLayer.getEntityGlint());
//		for (RenderLayer layer : EXTRA_RENDER_LAYERS) {
//			assign(buffers, layer);
//		}
		imm = new BatcherVertexConsumerProvider();
		unlitFacade = new UnlitFacade(imm);
	}

//	private void assign(Map<RenderLayer, BufferAllocator> buffers, RenderLayer layer) {
//		buffers.put(layer, new BufferAllocator(layer.getExpectedBufferSize()));
//	}

	public boolean isPopulated() {
		return populated;
	}

	public void repopulate() {
		dirty = true;
	}

	public void begin(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		if (dirty) {
			populated = false;
			dirty = false;
			spritesToUpdate.clear();
			buffers.clear();
		}
	}

	public void render(Batchable batchable, DrawContext draw, int x, int y, float delta) {
		if (!populated) {
			try {
				batchable.renderForBatch(batchable.isSideLit() ? imm : unlitFacade, draw, x-this.x, y+this.y, z, delta);
			} catch (Throwable t) {
				if (EmiConfig.devMode) {
					EmiLog.error("Batchable threw exception during batched rendering. See log for info", t);
				}
				batchable.setUnbatchable();
			}
		}
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta) {
		render(stack, draw, x, y, delta, -1 ^ EmiIngredient.RENDER_AMOUNT);
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta, int flags) {
		if (stack instanceof Batchable b && !b.isUnbatchable() && isEnabled() && (flags & EmiIngredient.RENDER_ICON) != 0) {
			if (!populated) {
				try {
					b.renderForBatch(b.isSideLit() ? imm : unlitFacade, draw, x - this.x, y + this.y, z, delta);
					if (/*sodiumSpriteHandle != null && */!stack.isEmpty()) {
						ItemStack is = stack.getEmiStacks().get(0).getItemStack();
						Minecraft client = Minecraft.getMinecraft();
						IBakedModel model = client.getRenderItem().getItemModelWithOverrides(is, null, null);
						if (model != null) {
							List<BakedQuad> quads = EmiPort.getQuads(model);
							for (BakedQuad quad : quads) {
								if (quad != null) {
									spritesToUpdate.addAll(quads);
								}
							}
						}
					}
				} catch (Throwable t) {
					if (EmiConfig.devMode) {
						EmiLog.error("Stack threw exception during batched rendering. See log for info", t);
					}
					b.setUnbatchable();
				}
			}
			stack.render(draw, x, y, delta, flags & (~EmiIngredient.RENDER_ICON));
		} else {
			stack.render(draw, x, y, delta, flags);
		}
	}

	public void draw() {
		if (!isEnabled()) {
			return;
		}
//		if (sodiumSpriteHandle != null) {
//			try {
//				for (Sprite sprite : spritesToUpdate) {
//					sodiumSpriteHandle.invoke(sprite);
//				}
//			} catch (Throwable t) {
//			}
//		}
		if (!populated) {
			bake();
//			populated = true;
		}
		RenderSystem.enableDepthTest();
		RenderHelper.enableGUIStandardItemLighting();
		Matrix4f mat = new Matrix4f();
		mat.mul(new Matrix4f().translation(x, y, 0));
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		for (Map.Entry<RenderLayer, ByteBuffer> en : buffers.entrySet()) {
			en.getKey().startDrawing();
			drawBuffer(en.getValue());
			en.getKey().endDrawing();
		}
//		BufferRenderer.reset();
	}

	private void bake() {
		imm.drawCurrentLayer();
//		buffers.values().forEach(VertexBuffer::deleteGlBuffers);
		buffers.clear();
		for (Map.Entry<RenderLayer, BufferBuilder> entry : imm.pending.entrySet()) {
			bake(entry.getKey(), entry.getValue());
		}
		imm.pending.clear();
	}

	public void bake(RenderLayer layer, BufferBuilder bldr) {
		bldr.finishDrawing();
		ByteBuffer src = bldr.getByteBuffer();
		ByteBuffer copy = ByteBuffer.allocateDirect(src.remaining());
		copy.put(src);
		copy.flip();
		buffers.put(layer, copy);
	}

	private void drawBuffer(ByteBuffer data) {
		BufferBuilder builder = Tessellator.getInstance().getBuffer();
		builder.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		builder.getByteBuffer().put(data.duplicate());
		builder.finishDrawing();
		OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
		Tessellator.getInstance().draw();
	}

	// Apparently BufferBuilder leaks memory in vanilla. Go figure
	public static class ClaimedCollection {
		private Set<StackBatcher> claimed = Sets.newHashSet();
		private List<StackBatcher> unclaimed = Lists.newArrayList();

		public StackBatcher claim() {
			synchronized (this) {
				StackBatcher batcher;
				if (unclaimed.isEmpty()) {
					batcher = new StackBatcher();
				} else {
					batcher = unclaimed.remove(unclaimed.size() - 1);
				}
				if (batcher == null) {
					batcher = new StackBatcher();
				}
				claimed.add(batcher);
				return batcher;
			}
		}

		public void unclaim(StackBatcher batcher) {
			synchronized (this) {
				claimed.remove(batcher);
				unclaimed.add(batcher);
			}
		}

		public void unclaimAll() {
			synchronized (this) {
				for (StackBatcher batcher : claimed) {
					unclaimed.add(batcher);
				}
				claimed.clear();
			}
		}
	}

	/*
	 * This class is mostly a copy of a 1.21 implementation of VertexConsumerProvider.Immediate
	 * The reimplementation allows compatibility with shader mods, as well as less hackery.
	 */
	private static class BatcherVertexConsumerProvider implements VertexConsumerProvider {
		private final Map<RenderLayer, BufferBuilder> pending = new HashMap<>();
		private RenderLayer currentLayer = null;

		@Override
		public VertexConsumer getBuffer(RenderLayer renderLayer) {
			BufferBuilder bufferBuilder = pending.get(renderLayer);

			if (bufferBuilder == null) {
				bufferBuilder = new BufferBuilder(2097152);
				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
				pending.put(renderLayer, bufferBuilder);
			}
			currentLayer = renderLayer;
			return new BufferBuilderConsumer(bufferBuilder);
		}

		public void drawCurrentLayer() {
			currentLayer = null;
		}
	}

	private static class UnlitFacade implements VertexConsumerProvider {
		private final BatcherVertexConsumerProvider delegate;
		private final IdentityHashMap<VertexConsumer, VertexConsumer> cache = new IdentityHashMap<>();

		public UnlitFacade(BatcherVertexConsumerProvider delegate) {
			this.delegate = delegate;
		}

		@Override
		public VertexConsumer getBuffer(RenderLayer layer) {
			return cache.computeIfAbsent(delegate.getBuffer(layer), Consumer::new);
		}

		private static final class Consumer implements VertexConsumer {
			private final VertexConsumer delegate;

			private Consumer(VertexConsumer delegate) {
				this.delegate = delegate;
			}

			@Override
			public VertexConsumer normal(float x, float y, float z) {
				delegate.normal(0, -1, 0); // this is the change
				return this;
			}

			// all other methods are direct delegation

			@Override
			public VertexConsumer vertex(float x, float y, float z) {
				delegate.vertex(x, y, z);
				return this;
			}

			@Override
			public VertexConsumer texture(float u, float v) {
				delegate.texture(u, v);
				return this;
			}

			@Override
			public VertexConsumer overlay(int u, int v) {
				delegate.overlay(u, v);
				return this;
			}

			@Override
			public VertexConsumer light(int u, int v) {
				delegate.light(u, v);
				return this;
			}

			@Override
			public VertexConsumer color(int r, int g, int b, int a) {
				delegate.color(r, g, b, a);
				return this;
			}

		}
	}

}
