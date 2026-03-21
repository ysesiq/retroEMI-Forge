//package dev.emi.emi.jemi;
//
//import dev.emi.emi.EmiPort;
//import dev.emi.emi.api.EmiApi;
//import dev.emi.emi.api.recipe.EmiRecipeCategory;
//import dev.emi.emi.api.stack.EmiIngredient;
//import dev.emi.emi.runtime.EmiDrawContext;
//import mezz.jei.api.gui.IDrawable;
//import mezz.jei.api.recipe.IRecipeCategory;
//import net.minecraft.client.Minecraft;
//import shim.net.minecraft.text.Text;
//
//import java.util.List;
//
//public class JemiCategory extends EmiRecipeCategory {
//	public IRecipeCategory<?> category;
//
//	public JemiCategory(IRecipeCategory<?> category) {
//		super(EmiPort.id(category.getUid()), (raw, x, y, delta) -> {});
//		this.icon = (raw, x, y, delta) -> {
//			EmiDrawContext context = EmiDrawContext.wrap(raw);
//			IDrawable icon = category.getIcon();
//			if (icon != null) {
//				icon.draw(Minecraft.getMinecraft(), x + (16 - icon.getWidth()) / 2, y + (16 - icon.getHeight()) / 2);
//			} else {
//				List<EmiIngredient> workstations = EmiApi.getRecipeManager().getWorkstations(this);
//				if (!workstations.isEmpty()) {
//					workstations.get(0).render(context.raw(), x, y, delta, EmiIngredient.RENDER_ICON);
//				} else {
//					String title = category.getTitle();
//					context.drawCenteredTextWithShadow(EmiPort.literal(title.substring(0, Math.min(2, title.length()))), x + 8, y + 2);
//				}
//			}
//		};
//		this.simplified = this.icon;
//		this.category = category;
//	}
//
//	@Override
//	public Text getName() {
//		return Text.literal(category.getTitle());
//	}
//}
