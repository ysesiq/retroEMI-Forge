package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.registry.GameData;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.StackBatcher;
import dev.emi.emi.api.render.EmiRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.registry.tag.ItemKey;
import net.minecraft.util.ResourceLocation;
import com.rewindmc.retroemi.ItemStacks;
import com.rewindmc.retroemi.RetroEMI;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@ApiStatus.Internal
public class ItemEmiStack extends EmiStack implements StackBatcher.Batchable {
	private static final Minecraft client = Minecraft.getMinecraft();

	private final Item item;
	private final int subtype;
    private final NBTTagCompound componentChanges;

	private boolean unbatchable;

	public ItemEmiStack(ItemStack stack) {
		this(stack, stack.stackSize);
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
        return amount == 0 || ItemStacks.isEmpty(getItemStack());
    }

    @Override
    public NBTTagCompound getNbt() {
        return componentChanges;
    }

//    @Override
//    public <T> @Nullable T get(NBTBase type) {
//        // Check the changes first
//        var changedOpt = this.componentChanges.get(type);
//        //noinspection OptionalAssignedToNull
//        if(changedOpt != null) {
//            return changedOpt.orElse(null);
//        }
//        // Check the item's default components
//        return this.item.getComponents().get(type);
//    }


    @Override
    public Object getKey() {
        return new ItemKey(item, subtype);
    }

    @Override
    public ResourceLocation getId() {
        return EmiPort.id(EmiPort.getItemRegistry().getNameForObject(item));
    }

    @Override
    public int hashCode() {
        if (item == null) {
            return 0;
        }
        final int prime = 31;
        int result = 1;
        result = prime * result + Item.getIdFromItem(item);
        result = prime * result + (item.getHasSubtypes() ? getItemStack().getItemDamage() : 0);
        return result;
    }

    @Override
    public void render(DrawContext draw, int x, int y, float delta, int flags) {
        EmiDrawContext context = EmiDrawContext.wrap(draw);
        ItemStack stack = getItemStack();
        if ((flags & RENDER_ICON) != 0) {
            glEnable(GL_RESCALE_NORMAL);
            context.enableDepthTest();
            RenderHelper.enableGUIStandardItemLighting();
            if (stack.getItem() instanceof ItemBlock && stack.getItemDamage() == 32767) stack.setItemDamage(0);
            draw.drawItem(stack, x, y);
            draw.drawItemInSlot(Minecraft.getMinecraft().fontRenderer, stack, x, y);
//            RenderHelper.disableStandardItemLighting();
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
        return RetroEMI.isSideLit(getItemStack());
    }

    @Override
    public boolean isUnbatchable() {
        ItemStack stack = getItemStack();
        return unbatchable || stack.isItemEnchanted() || stack.isItemDamaged() || !EmiAgnos.canBatch(stack);
    }

    @Override
    public void setUnbatchable() {
        this.unbatchable = true;
    }

    @Override
    public void renderForBatch(DrawContext draw, int x, int y, int z, float delta) {
        //        EmiDrawContext context = EmiDrawContext.wrap(draw);
        //        ItemStack stack = getItemStack();
        //        ItemRenderer ir = client.getItemRenderer();
        //        BakedModel model = ir.getModel(stack, null, null, 0);
        //        context.push();
        //        try {
        //            context.matrices().translate(x, y, 100.0f + z + (model.hasDepth() ? 50 : 0));
        //            context.matrices().translate(8.0, 8.0, 0.0);
        //            context.matrices().scale(16.0f, 16.0f, 16.0f);
        //            ir.renderItem(stack, ModelTransformationMode.GUI, false, context.matrices(), vcp, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV, model);
        //        } finally {
        //            context.pop();
        //        }
    }

    @Override
    public List<Text> getTooltipText() {
        return ((List<String>) getItemStack().getTooltip(client.thePlayer, TooltipContext.BASIC))
            .stream().map(Text::literal).collect(Collectors.toList());
    }

    @Override
    public List<TooltipComponent> getTooltip() {
        ItemStack stack = getItemStack();
        List<TooltipComponent> list = Lists.newArrayList();
        if (!isEmpty()) {
            list.addAll(EmiAgnos.getItemTooltip(stack));
            //String namespace = EmiPort.getItemRegistry().getNameForObject(stack.getItem());
            //String mod = EmiUtil.getModName(namespace);
            //list.add(TooltipComponent.of(Text.literal(mod).formatted(Formatting.BLUE, Formatting.ITALIC)));
            if (EmiConfig.appendModId || EmiConfig.appendItemModId) {
                String stackNamespace = GameData.getItemRegistry().getNameForObject(stack.getItem());
                String modNamespaceBase = stackNamespace.replaceAll(":.*", "");
                String modNamespace = modNamespaceBase.substring(0, 1).toUpperCase() + modNamespaceBase.substring(1);
                list.add(TooltipComponent.of(Text.literal(modNamespace).formatted(Formatting.BLUE, Formatting.ITALIC)));
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
        return Text.translatable(getItemStack().getUnlocalizedName() + ".name");
    }

    @Override
    public int getSubtype() {
        return this.subtype;
    }

	static class ItemEntry {
	}
}
