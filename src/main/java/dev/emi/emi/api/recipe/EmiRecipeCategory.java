package dev.emi.emi.api.recipe;

import java.util.Comparator;
import java.util.List;

import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiRecipeCategoryProperties;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.text.Text;
import shim.net.minecraft.util.Formatting;
import net.minecraft.util.ResourceLocation;

public class EmiRecipeCategory implements EmiRenderable {
	public ResourceLocation id;
	public EmiRenderable icon, simplified;
	@ApiStatus.Experimental
	public Comparator<EmiRecipe> sorter;

	/**
	 * A constructor to use only a single renderable for both the icon and simplified icon.
	 * It is generally recommended that simplified icons be unique and follow the style of vanilla icons.
	 * <p>
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	public EmiRecipeCategory(ResourceLocation id, EmiRenderable icon) {
		this(id, icon, icon);
	}

	/**
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	public EmiRecipeCategory(ResourceLocation id, EmiRenderable icon, EmiRenderable simplified) {
		this(id, icon, simplified, EmiRecipeSorting.none());
	}

	/**
	 * {@link EmiStack} instances can be passed as {@link EmiRenderable}
	 */
	@ApiStatus.Experimental
	public EmiRecipeCategory(ResourceLocation id, EmiRenderable icon, EmiRenderable simplified, Comparator<EmiRecipe> sorter) {
		this.id = id;
		this.icon = icon;
		this.simplified = simplified;
		this.sorter = sorter;
	}

	public Text getName() {
		return EmiPort.translatable(EmiUtil.translateId("emi.category.", getId()));
	}

	public ResourceLocation getId() {
		return id;
	}

	@Override
	public void render(DrawContext draw, int x, int y, float delta) {
		EmiRecipeCategoryProperties.getIcon(this).render(draw, x, y, delta);
	}

	public void renderSimplified(DrawContext draw, int x, int y, float delta) {
		EmiRecipeCategoryProperties.getSimplifiedIcon(this).render(draw, x, y, delta);
	}

	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		list.add(TooltipComponent.of(EmiPort.ordered(getName())));
		if (EmiUtil.showAdvancedTooltips()) {
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(id.toString(), EnumChatFormatting.DARK_GRAY))));
		}
		if (EmiConfig.appendModId) {
			list.add(TooltipComponent.of(EmiPort.ordered(EmiPort.literal(EmiUtil.getModName(getId().getResourceDomain()),
                EnumChatFormatting.BLUE, EnumChatFormatting.ITALIC))));
		}
		return list;
	}

	@ApiStatus.Experimental
	public @Nullable Comparator<EmiRecipe> getSort() {
		return sorter;
	}
}
