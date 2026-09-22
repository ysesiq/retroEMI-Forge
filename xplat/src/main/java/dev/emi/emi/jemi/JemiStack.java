package dev.emi.emi.jemi;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IIngredientType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.item.TooltipType;
import shim.net.minecraft.text.Text;
import shim.net.minecraft.util.Formatting;

public class JemiStack<T> extends EmiStack {
	public final IIngredientType<T> type;
	private final IIngredientHelper<T> helper;
	public final Object base;
	public final T ingredient;
	public IIngredientRenderer<T> renderer;

	public JemiStack(IIngredientType<T> type, IIngredientHelper<T> helper, IIngredientRenderer<T> renderer, T ingredient) {
		this.type = type;
		this.helper = helper;
		this.renderer = renderer;
		this.ingredient = ingredient;
		base = helper.getUniqueId(ingredient);
	}

	public String getJeiUid() {
		return helper.getUniqueId(ingredient);
	}

	@Override
	public void render(DrawContext raw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		context.enableBlend();
		renderer.render(Minecraft.getMinecraft(), x, y, ingredient);
		context.pop();
	}

	@Override
	public JemiStack<T> copy() {
		return new JemiStack<T>(type, helper, renderer, helper.copyIngredient(ingredient));
	}

	@Override
	public boolean isEmpty() {
		return !helper.isValidIngredient(ingredient);
	}

	@Override
	public NBTTagCompound getNbt() {
		return null;
	}

	@Override
	public Object getKey() {
		return base;
	}

	@Override
	public ResourceLocation getId() {
		return EmiPort.id(helper.getResourceId(ingredient));
	}

	@Override
	public List<Text> getTooltipText() {
		return renderer.getTooltip(Minecraft.getMinecraft(), ingredient, TooltipType.BASIC).stream().map(EmiPort::literal).collect(Collectors.toList());
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		Minecraft client = Minecraft.getMinecraft();
		List<String> tooltip = renderer.getTooltip(client, ingredient, client.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
		for (String s : tooltip) {
			list.add(TooltipComponent.of(EmiPort.literal(s)));
		}

		ResourceLocation id = getId();
		if (EmiConfig.appendModId && id != null) {
			String mod = EmiUtil.getModName(id.getNamespace());
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(mod, Formatting.BLUE, Formatting.ITALIC))));
		}

		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public int getSubtype() {
		return 0;
	}

	@Override
	public Text getName() {
		return EmiPort.literal(helper.getDisplayName(ingredient));
	}
}
