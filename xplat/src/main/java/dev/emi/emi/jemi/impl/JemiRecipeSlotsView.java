//package dev.emi.emi.jemi.impl;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import com.google.common.collect.Lists;
//
//import mezz.jei.api.gui.ingredient.IRecipeSlotView;
//import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
//import shim.mezz.jei.api.recipe.RecipeIngredientRole;
//
//public class JemiRecipeSlotsView implements IRecipeSlotsView {
//	private List<IRecipeSlotView> slots = Lists.newArrayList();
//
//	public JemiRecipeSlotsView(List<JemiRecipeSlot> slots) {
//		this.slots.addAll(slots);
//	}
//
//	@Override
//	public List<IRecipeSlotView> getSlotViews() {
//		return slots;
//	}
//
//	@Override
//	public List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role) {
//		return slots.stream().filter(s -> Objects.equals(s.getRole(), role)).collect(Collectors.toList());
//	}
//
//	@Override
//	public Optional<IRecipeSlotView> findSlotByName(String slotName) {
//		for (IRecipeSlotView slot : slots) {
//			if (Objects.equals(slot.getSlotName().orElseGet(() -> null), slotName)) {
//				return Optional.of(slot);
//			}
//		}
//		return Optional.empty();
//	}
//}
