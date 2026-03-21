//package dev.emi.emi.jemi;
//
//import com.google.common.collect.Lists;
//import dev.emi.emi.EmiPort;
//import dev.emi.emi.EmiUtil;
//import dev.emi.emi.api.stack.EmiStack;
//import dev.emi.emi.config.EmiConfig;
//import dev.emi.emi.jemi.impl.JemiTooltipBuilder;
//import dev.emi.emi.runtime.EmiDrawContext;
//import mezz.jei.api.ingredients.IIngredientHelper;
//import mezz.jei.api.ingredients.IIngredientRenderer;
//import mezz.jei.api.recipe.IIngredientType;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.util.ITooltipFlag;
//import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.text.TextFormatting;
//import shim.net.minecraft.client.gui.DrawContext;
//import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
//import shim.net.minecraft.text.Text;
//
//
//import java.util.List;
//
//public class JemiStack<T> extends EmiStack {
//	private final IIngredientType<T> type;
//	private final IIngredientHelper<T> helper;
//	public final Object base;
//	public final T ingredient;
//	public IIngredientRenderer<T> renderer;
//
//	public JemiStack(IIngredientType<T> type, IIngredientHelper<T> helper, IIngredientRenderer<T> renderer, T ingredient) {
//		this.type = type;
//		this.helper = helper;
//		this.renderer = renderer;
//		this.ingredient = ingredient;
//		if (type instanceof IIngredientTypeWithSubtypes<?, T> iitws) {
//			base = iitws.getBase(ingredient);
//		} else {
//			base = helper.getUniqueId(ingredient, UidContext.Recipe);
//		}
//	}
//
//	public String getJeiUid() {
//		return helper.getUniqueId(ingredient, UidContext.Ingredient);
//	}
//
//	@Override
//	public void render(DrawContext raw, int x, int y, float delta, int flags) {
//		EmiDrawContext context = EmiDrawContext.wrap(raw);
//		int xOff = (16 - renderer.getWidth()) / 2;
//		int yOff = (16 - renderer.getHeight()) / 2;
//		context.push();
//		context.matrices().translate(x + xOff, y + yOff, 0);
//		renderer.render(context.raw(), ingredient);
//		context.pop();
//	}
//
//	@Override
//	public JemiStack<T> copy() {
//		return new JemiStack<T>(type, helper, renderer, helper.copyIngredient(ingredient));
//	}
//
//	@Override
//	public boolean isEmpty() {
//		return !helper.isValidIngredient(ingredient);
//	}
//
//	@Override
//	public NBTTagCompound getComponentChanges() {
//		return NBTTagCompound.EMPTY;
//	}
//
//	@Override
//	public Object getKey() {
//		return base;
//	}
//
//	@Override
//	public ResourceLocation getId() {
//		return helper.getResourceId(ingredient);
//	}
//
//	@Override
//	public List<Text> getTooltipText() {
//		return renderer.getTooltip(ingredient, ITooltipFlag.TooltipFlags.NORMAL);
//	}
//
//	@Override
//	public List<TooltipComponent> getTooltip() {
//		List<TooltipComponent> list = Lists.newArrayList();
//		Minecraft client = Minecraft.getMinecraft();
//		JemiTooltipBuilder builder = new JemiTooltipBuilder();
//		renderer.getTooltip(builder, ingredient, client.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
//		list.addAll(builder.tooltip);
//
//		ResourceLocation id = getId();
//		if (EmiConfig.appendModId && id != null) {
//			String mod = EmiUtil.getModName(id.getNamespace());
//			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(mod, TextFormatting.BLUE, TextFormatting.ITALIC))));
//		}
//
//		list.addAll(super.getTooltip());
//		return list;
//	}
//
//	@Override
//	public Text getName() {
//		return EmiPort.literal(helper.getDisplayName(ingredient));
//	}
//}
