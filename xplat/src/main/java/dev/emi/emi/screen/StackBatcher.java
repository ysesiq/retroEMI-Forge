package dev.emi.emi.screen;

import dev.emi.emi.api.stack.EmiIngredient;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.render.VertexConsumerProvider;

public class StackBatcher {

	public interface Batchable {
		boolean isSideLit();

		boolean isUnbatchable();

		void setUnbatchable();

		void renderForBatch(VertexConsumerProvider vcp, DrawContext draw, int x, int y, int z, float delta);
	}

	public StackBatcher() {
	}

	public boolean isPopulated() {
		return false;
	}

	public void repopulate() {
	}

	public void begin(int x, int y, int z) {
	}

	public void render(Batchable batchable, DrawContext draw, int x, int y, float delta) {
		if (batchable instanceof EmiIngredient ei) {
			render(ei, draw, x, y, delta);
		}
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta) {
		render(stack, draw, x, y, delta, -1 ^ EmiIngredient.RENDER_AMOUNT);
	}

	public void render(EmiIngredient stack, DrawContext draw, int x, int y, float delta, int flags) {
		stack.render(draw, x, y, delta, flags);
	}

	public void draw() {
	}

}
