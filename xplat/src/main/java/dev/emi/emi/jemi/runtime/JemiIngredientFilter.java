//package dev.emi.emi.jemi.runtime;
//
//import com.google.common.collect.ImmutableList;
//import dev.emi.emi.api.EmiApi;
//import dev.emi.emi.jemi.JemiUtil;
//import dev.emi.emi.screen.EmiScreenManager;
//import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
//import mezz.jei.api.IIngredientFilter;
//import mezz.jei.api.recipe.IIngredientType;
//
//import java.util.List;
//import java.util.Optional;
//
//public class JemiIngredientFilter implements IIngredientFilter {
//
//	@Override
//	public void setFilterText(String filterText) {
//		EmiApi.setSearchText(filterText);
//	}
//
//	@Override
//	public String getFilterText() {
//		return EmiApi.getSearchText();
//	}
//
//	@Override
//	public ImmutableList<Object> getFilteredIngredients() {
//		SidebarPanel search = EmiScreenManager.getSearchPanel();
//		if (search == null || search.space == null) {
//			return List.of();
//		}
//		return search.space.getStacks().stream()
//			.map(i -> JemiUtil.getTyped(i.getEmiStacks().get(0)))
//			.filter(Optional::isPresent).map(Optional::get)
//			.map(i -> i.getIngredient(ingredientType))
//			.filter(Optional::isPresent).map(Optional::get)
//			.toList();
//	}
//}
