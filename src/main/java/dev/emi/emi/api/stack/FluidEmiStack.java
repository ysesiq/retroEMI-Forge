package dev.emi.emi.api.stack;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.tooltip.EmiTextTooltipWrapper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

@ApiStatus.Internal
public class FluidEmiStack extends EmiStack {
	private final Fluid fluid;
	private final NBTTagCompound componentChanges;

	public FluidEmiStack(Fluid fluid) {
		this(fluid, null);
	}

	public FluidEmiStack(Fluid fluid, NBTTagCompound componentChanges) {
		this(fluid, componentChanges, 0);
	}

	public FluidEmiStack(Fluid fluid, NBTTagCompound componentChanges, long amount) {
		this.fluid = fluid;
		this.componentChanges = componentChanges;
		this.amount = amount;
	}

	@Override
	public EmiStack copy() {
		EmiStack e = new FluidEmiStack(fluid, componentChanges, amount);
		e.setChance(chance);
		e.setRemainder(getRemainder().copy());
		e.comparison = comparison;
		return e;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public NBTTagCompound getNbt() {
		return componentChanges;
	}

	@Override
	public Object getKey() {
		return fluid;
	}

	@Override
	public ResourceLocation getId() {
		return EmiPort.id(fluid.getName());
	}

	@Override
	public void render(DrawContext raw, int x, int y, float delta, int flags) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		if ((flags & RENDER_ICON) != 0) {
			context.push();
			context.matrices().translate(0, 0, 100);
			EmiAgnos.renderFluid(this, context.matrices(), x, y, delta);
			context.pop();
		}
		if ((flags & RENDER_REMAINDER) != 0) {
			EmiRender.renderRemainderIcon(this, context.raw(), x, y);
		}
	}

	@Override
	public List<Text> getTooltipText() {
		return EmiAgnos.getFluidTooltip(fluid, componentChanges);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> list = Lists.newArrayList();
		List<Text> text = getTooltipText();
		if (!text.isEmpty()) {
			list.add(new EmiTextTooltipWrapper(this, EmiPort.ordered(text.get(0))));
		}
		list.addAll(text.stream().skip(1).map(EmiTooltipComponents::of).collect(Collectors.toList()));
		if (amount > 1) {
			list.add(EmiTooltipComponents.getAmount(this));
		}
		String namespace = this.getId().getResourceDomain();
		EmiTooltipComponents.appendModName(list, namespace);
		list.addAll(super.getTooltip());
		return list;
	}

	@Override
	public Text getName() {
		return EmiAgnos.getFluidName(fluid, componentChanges);
	}

    @Override
    public int getSubtype() {
        return 0;
    }

    static class FluidEntry {
	}
}
