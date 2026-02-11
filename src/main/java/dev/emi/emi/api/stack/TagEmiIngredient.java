package dev.emi.emi.api.stack;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiTagKey;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import dev.emi.emi.screen.tooltip.RemainderTooltipComponent;
import dev.emi.emi.screen.tooltip.TagTooltipComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Formatting;
import net.minecraft.util.ResourceLocation;

@ApiStatus.Internal
public class TagEmiIngredient implements EmiIngredient {
	private final ResourceLocation id;
	private List<EmiStack> stacks;
	public final TagKey<?> key;
	private final EmiTagKey<?> tagKey;
	private long amount;
	private float chance = 1;

	@ApiStatus.Internal
	public TagEmiIngredient(TagKey<?> key, long amount) {
		this(EmiTagKey.of(key), amount);
	}

	@ApiStatus.Internal
	public TagEmiIngredient(TagKey<?> key, List<EmiStack> stacks, long amount) {
		this(EmiTagKey.of(key), stacks, amount);
	}

	@ApiStatus.Internal
	public TagEmiIngredient(EmiTagKey<?> key, long amount) {
		this(key, EmiTags.getValues(key), amount);
	}

	private TagEmiIngredient(EmiTagKey<?> key, List<EmiStack> stacks, long amount) {
		this.id = key.id();
		this.key = key.raw();
		this.tagKey = key;
		this.stacks = stacks;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TagEmiIngredient tag && tag.key.equals(this.key);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public EmiIngredient copy() {
		EmiIngredient stack = new TagEmiIngredient(tagKey, amount);
		stack.setChance(chance);
		return stack;
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return stacks;
	}

	@Override
	public long getAmount() {
		return amount;
	}

	@Override
	public EmiIngredient setAmount(long amount) {
		this.amount = amount;
		return this;
	}

	@Override
	public float getChance() {
		return chance;
	}

	@Override
	public EmiIngredient setChance(float chance) {
		this.chance = chance;
		return this;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		Minecraft client = Minecraft.getMinecraft();

//		if ((flags & RENDER_ICON) != 0) {
//			if (!tagKey.hasCustomModel()) {
//				if (stacks.size() > 0) {
//					stacks.get(0).render(context.raw(), x, y, delta, -1 ^ RENDER_AMOUNT);
//				}
//			} else {
				// TODO tag textures

//                BakedModel model = EmiAgnos.getBakedTagModel(EmiTags.getCustomModel(key));
//
//                context.matrices().push();
//                context.matrices().translate(x + 8, y + 8, 150);
//                context.matrices().multiplyPositionMatrix(new Matrix4f().scaling(1.0f, -1.0f, 1.0f));
//                context.matrices().scale(16.0f, 16.0f, 16.0f);
//
//                model.getTransformation().getTransformation(ModelTransformationMode.GUI).apply(false, context.matrices());
//                context.matrices().translate(-0.5f, -0.5f, -0.5f);
//
//                if (!model.isSideLit()) {
//                    DiffuseLighting.disableGuiDepthLighting();
//                }
//                VertexConsumerProvider.Immediate immediate = context.raw().getVertexConsumers();
//
//                ((ItemRendererAccessor) client.getItemRenderer())
//                    .invokeRenderBakedItemModel(model,
//                        ItemStack.EMPTY, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, context.matrices(),
//                        ItemRenderer.getDirectItemGlintConsumer(immediate,
//                            TexturedRenderLayers.getEntityTranslucentCull(), true, false));
//                immediate.draw();
//
//                if (!model.isSideLit()) {
//                    DiffuseLighting.enableGuiDepthLighting();
//                }
//
//                context.matrices().pop();

//                ResourceLocation texture = tagKey.getCustomModel();
//            System.out.println("resource texture:" + texture.toString());
//
//                GL11.glPushMatrix();
//                GL11.glTranslatef(x + 8, y + 8, 150);
//                GL11.glScalef(1.0f, -1.0f, 1.0f);
//                GL11.glScalef(16.0f, 16.0f, 16.0f);
//
//                GL11.glTranslatef(-0.5f, -0.5f, -0.5f);
//
//                RenderHelper.disableStandardItemLighting();
//
//                GL11.glEnable(GL11.GL_LIGHTING);
//                GL11.glEnable(GL11.GL_LIGHT0);
//                GL11.glEnable(GL11.GL_COLOR_MATERIAL);
//                GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
//
////                GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_POSITION,
////                    (FloatBuffer) FloatBuffer.wrap(new float[] {0.0f, 0.0f, 1.0f, 0.0f}));
////                GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_AMBIENT,
////                    (FloatBuffer) FloatBuffer.wrap(new float[] {0.4f, 0.4f, 0.4f, 1.0f}));
////                GL11.glLightfv(GL11.GL_LIGHT0, GL11.GL_DIFFUSE,
////                    (FloatBuffer) FloatBuffer.wrap(new float[] {0.6f, 0.6f, 0.6f, 1.0f}));
//
//                GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
//
//                GL11.glEnable(GL11.GL_BLEND);
//                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//                GL11.glEnable(GL11.GL_ALPHA_TEST);
//                GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
//
////                client.getTextureManager().bindTexture(texture);
//                RenderItem renderItem = new RenderItem();
//                renderItem.renderItemAndEffectIntoGUI(client.fontRenderer, client.getTextureManager(), new ItemStack((Item) GameData.getItemRegistry().getObject(texture)), x, y);
////			}
//		}
		if ((flags & RENDER_AMOUNT) != 0) {
			String count = "";
			if (amount != 1) {
				count += amount;
			}
			EmiRenderHelper.renderAmount(context, x, y, EmiPort.literal(count));
		}
		if ((flags & RENDER_INGREDIENT) != 0) {
			EmiRender.renderTagIcon(this, context.raw(), x, y);
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, context.raw(), x, y);
		}
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		list.add(new EmiTextTooltipWrapper(this, EmiPort.ordered(tagKey.getTagName())));
		if (EmiUtil.showAdvancedTooltips()) {
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal("#" + id, Formatting.DARK_GRAY))));
		}
//		if (tagKey.isOf(EmiPort.getFluidRegistry()) && amount > 1) {
//			list.add(TooltipComponent.of(EmiPort.ordered(EmiRenderHelper.getAmountText(this, amount))));
//		}
		if (EmiConfig.appendModId) {
			String mod = EmiUtil.getModName(id.getResourceDomain());
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC))));
		}
		list.add(new TagTooltipComponent(stacks));
		for (EmiStack stack : stacks) {
			if (!stack.getRemainder().isEmpty()) {
				list.add(new RemainderTooltipComponent(this));
				break;
			}
		}
		return list;
	}
}
