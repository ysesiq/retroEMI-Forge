package dev.emi.emi.api.stack;

import java.util.List;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.item.TooltipContext;
import shim.net.minecraft.client.render.VertexConsumerProvider;
import shim.net.minecraft.registry.tag.ItemKey;
import shim.net.minecraft.text.Text;
import shim.net.minecraft.util.Formatting;


@ApiStatus.Internal
public class ItemEmiStack extends EmiStack implements StackBatcher.Batchable {
	private static final Minecraft client = Minecraft.getMinecraft();

	private final Item item;
	private final int subtype;
	private final NBTTagCompound componentChanges;

	private boolean unbatchable;

	public ItemEmiStack(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemEmiStack(ItemStack stack, long amount) {
		this(stack.getItem(), stack.getTagCompound(), amount, stack.getItemDamage());
	}

	public ItemEmiStack(Item item, NBTTagCompound components, long amount, int subtype) {
		this.item = item;
		this.componentChanges = components;
		this.subtype = subtype;
		this.amount = amount;
	}

	@Override
	public ItemStack getItemStack() {
		ItemStack stack = new ItemStack(this.item, (int) this.amount, this.subtype);
		if (this.componentChanges != null) {
			stack.setTagCompound(this.componentChanges);
		}
		return stack;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new ItemEmiStack(item, componentChanges, amount, subtype);
		e.setChance(chance);
		e.setRemainder(getRemainder().copy());
		e.comparison = comparison;
		return e;
	}

	@Override
	public boolean isEmpty() {
		return amount == 0 || getItemStack().isEmpty();
	}

	@Override
	public NBTTagCompound getComponentChanges() {
		return componentChanges;
	}

//	@Override
//	public <T> @Nullable T get(NBTBase type) {
//		// Check the changes first
//		var changedOpt = this.componentChanges.get(type);
//		//noinspection OptionalAssignedToNull
//		if(changedOpt != null) {
//			return changedOpt.orElse(null);
//		}
//		// Check the item's default components
//		return this.item.getComponents().get(type);
//	}


	@Override
	public Object getKey() {
		return new ItemKey(item, subtype);
	}

	@Override
	public ResourceLocation getId() {
		return EmiPort.getItemRegistry().getNameForObject(item);
	}

	@Override
	public int hashCode() {
		if (item == null) {
			return 0;
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + Item.getIdFromItem(item);
		result = prime * result + getSubtype();
		return result;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		ItemStack stack = getItemStack();
		if ((flags & RENDER_ICON) != 0) {
			GlStateManager.enableRescaleNormal();
			context.enableDepthTest();
			RenderHelper.enableGUIStandardItemLighting();
			if (stack.getItemDamage() == 32767) stack.setItemDamage(0);
			draw.drawItem(stack, x, y);
			draw.drawItemInSlot(Minecraft.getMinecraft().fontRenderer, stack, x, y);
			RenderHelper.disableStandardItemLighting();
		}
		if ((flags & RENDER_AMOUNT) != 0) {
			String count = "";
			if (amount != 1) {
				count += amount;
			}
			EmiRenderHelper.renderAmount(context, x, y, EmiPort.literal(count));
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, context.raw(), x, y);
		}
	}

	@Override
	public boolean isSideLit() {
		return client.getRenderItem().getItemModelWithOverrides(getItemStack(), null, null).isGui3d();
	}

	@Override
	public boolean isUnbatchable() {
		ItemStack stack = getItemStack();
		return unbatchable || stack.isItemEnchanted() || stack.isItemDamaged() || !EmiAgnos.canBatch(stack)
			|| client.getRenderItem().getItemModelWithOverrides(getItemStack(), null, null).isBuiltInRenderer();
	}

	@Override
	public void setUnbatchable() {
		this.unbatchable = true;
	}

	@Override
	public void renderForBatch(VertexConsumerProvider vcp, DrawContext draw, int x, int y, int z, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		ItemStack stack = getItemStack();
		RenderItem ir = client.getRenderItem();
		IBakedModel model = ir.getItemModelWithOverrides(stack, null, null);
		context.push();
		try {
			context.matrices().translate(x, y, 100.0f + z + 0);
			context.matrices().translate(8.0, 8.0, 0.0);
			context.matrices().scale(16.0f, 16.0f, 16.0f);
			ir.renderItem(stack, model);
		} finally {
			context.pop();
		}
	}

	@Override
	public List<Text> getTooltipText() {
		return RetroEMI.getItemToolTip(getItemStack(), TooltipContext.BASIC);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		ItemStack stack = getItemStack();
		List<TooltipComponent> list = Lists.newArrayList();
		if (!isEmpty()) {
			list.addAll(EmiAgnos.getItemTooltip(stack));
			if (!list.isEmpty() && list.get(0) instanceof OrderedTextTooltipComponent ottc) {
				list.set(0, new EmiTextTooltipWrapper(this, ottc));
			}
			if (EmiConfig.appendItemModId && EmiConfig.appendModId && Thread.currentThread() != EmiSearch.searchThread) {
				String namespace = EmiPort.getItemRegistry().getNameForObject(stack.getItem()).getNamespace();
				String mod = EmiUtil.getModName(namespace);
				list.add(TooltipComponent.of(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC)));
			}
			list.addAll(super.getTooltip());
		}
		return list;
	}

	@Override
	public Text getName() {
		if (isEmpty()) {
			return EmiPort.literal("");
		}
		return Text.literal(getItemStack().getDisplayName());
	}

	@Override
	public int getSubtype() {
		return this.subtype;
	}

	static class ItemEntry {
	}
}
