package dev.emi.emi.nemi;

import java.awt.Point;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.Recipe.RecipeId;
import codechicken.nei.recipe.TemplateRecipeHandler;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class NemiRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final TemplateRecipeHandler neiHandler;
    private final int recipeIndex;
	private static final Set<TemplateRecipeHandler> HANDLERS = Collections.newSetFromMap(new WeakHashMap<>());
	private final List<PositionedStack> ingredientStacks;
    private final PositionedStack resultStack;
    private final List<PositionedStack> otherStacks;

    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public NemiRecipe(EmiRecipeCategory category, TemplateRecipeHandler neiHandler, int recipeIndex, ResourceLocation id) {
        this.category = category;
        this.neiHandler = neiHandler;
        this.recipeIndex = recipeIndex;
        this.id = id;

        this.ingredientStacks = safeList(neiHandler.getIngredientStacks(recipeIndex));
        this.resultStack = neiHandler.getResultStack(recipeIndex);
        this.otherStacks = safeList(neiHandler.getOtherStacks(recipeIndex));

        this.inputs = parseInputs();
        this.outputs = parseOutputs();
        registerHandler(neiHandler);
    }

    private static List<PositionedStack> safeList(List<PositionedStack> stacks) {
        return stacks == null ? new ArrayList<>() : stacks;
    }

    private static PositionedStack safeStack(List<PositionedStack> stacks, int index) {
        if (stacks == null || index < 0 || index >= stacks.size()) return null;
        return stacks.get(index);
    }

    private List<EmiIngredient> parseInputs() {
        List<EmiIngredient> parsedInputs = new ArrayList<>();
        for (PositionedStack stack : ingredientStacks) {
            parsedInputs.add(parseIngredient(stack));
        }
        return parsedInputs;
    }

    private List<EmiStack> parseOutputs() {
        List<EmiStack> parsedOutputs = new ArrayList<>();

        addMainOutput(parsedOutputs);
        addSecondaryOutputs(parsedOutputs);

        return parsedOutputs;
    }

    private void addMainOutput(List<EmiStack> parsedOutputs) {
        if (resultStack != null && resultStack.item != null) {
            parsedOutputs.add(EmiStack.of(resultStack.item));
        }
    }

    private void addSecondaryOutputs(List<EmiStack> parsedOutputs) {
        for (PositionedStack stack : otherStacks) {
            if (stack != null && stack.item != null) {
                parsedOutputs.add(EmiStack.of(stack.item));
            }
        }
    }

    static EmiIngredient parseIngredient(PositionedStack positionedStack) {
        if (positionedStack == null) {
            return EmiStack.EMPTY;
        }

        List<EmiIngredient> ingredients = new ArrayList<>();

        if (positionedStack.items != null && positionedStack.items.length > 0) {
            for (ItemStack stack : positionedStack.items) {
                if (stack != null) {
                    // ofPotentialTag expands wildcard metadata used heavily by NEI
                    ingredients.add(EmiStack.ofPotentialTag(stack));
                }
            }
        } else if (positionedStack.item != null) {
            ingredients.add(EmiStack.ofPotentialTag(positionedStack.item));
        }

        if (ingredients.isEmpty()) {
            return EmiStack.EMPTY;
        }

        return EmiIngredient.of(ingredients);
    }

    public RecipeId getNeiRecipeId() {
        return RecipeId.of(neiHandler, recipeIndex);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

	@Override
	public int getDisplayWidth() {
		return Math.max(HandlerInfo.DEFAULT_WIDTH, GuiRecipeTab.getHandlerInfo(neiHandler).getWidth());
	}

	@Override
	public int getDisplayHeight() {
		HandlerInfo info = GuiRecipeTab.getHandlerInfo(neiHandler);
		int recipeHeight = 0;
//		try {
			recipeHeight = neiHandler.getRecipeHeight(recipeIndex);
//		} catch (Throwable ignored) {
//		recipeHeight = info.getHeight();
//		}
		int h = recipeHeight > 0 ? recipeHeight : info.getHeight();
		return h + info.getYShift() + 4;
	}

    @Override
    public boolean supportsRecipeTree() {
        return true;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        addBackgroundAndExtrasWidget(widgets);
        addInputWidgets(widgets);
        addMainOutputWidget(widgets);
        addSecondaryOutputWidgets(widgets);
        addRecipeTooltip(widgets);
    }

    private void addRecipeTooltip(WidgetHolder widgets) {
        widgets.addTooltip((mouseX, mouseY) -> {
            try {
                NemiGuiRecipe gui = NemiGuiRecipe.instance();
                if (gui == null) {
                    return new ArrayList<>();
                }
                Point global = GuiDraw.getMousePosition();
                gui.setRecipeOrigin(global.x - mouseX, global.y - mouseY);
                List<String> tooltip = neiHandler.handleTooltip(gui, new ArrayList<>(), recipeIndex);
                if (tooltip == null) {
                    return new ArrayList<>();
                }
                return tooltip.stream()
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isEmpty())
                        .map(EmiPort::literal)
                        .map(EmiPort::ordered)
                        .map(TooltipComponent::of)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }, 0, 0, getDisplayWidth(), getDisplayHeight());
    }

    private void addBackgroundAndExtrasWidget(WidgetHolder widgets) {
        EmiDrawContext context = EmiDrawContext.instance();
        widgets.addDrawable(0, 0, this.getDisplayWidth(), this.getDisplayHeight(), (raw, mouseX, mouseY, delta) -> {
            try {
                context.push();
                context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                context.enableBlend();

                neiHandler.drawBackground(recipeIndex);
                neiHandler.drawForeground(recipeIndex);
            } catch (Exception e) {
                EmiLog.error("Error drawing NEI background for recipe " + id, e);
            } finally {
                context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                context.pop();
            }
        });
    }

    private void addInputWidgets(WidgetHolder widgets) {
        List<PositionedStack> stacks = safeList(neiHandler.getIngredientStacks(recipeIndex));
        for (int i = 0; i < stacks.size(); i++) {
            PositionedStack stack = stacks.get(i);
            int index = i;
            widgets.add(new NemiSlotWidget(() -> safeStack(neiHandler.getIngredientStacks(recipeIndex), index), neiHandler, recipeIndex, stack.relx - 1, stack.rely - 1).drawBack(false));
        }
    }

    private void addMainOutputWidget(WidgetHolder widgets) {
        if (resultStack != null && resultStack.item != null) {
            widgets.add(new NemiSlotWidget(() -> neiHandler.getResultStack(recipeIndex), neiHandler, recipeIndex, resultStack.relx - 1, resultStack.rely - 1).drawBack(false).recipeContext(this));
        }
    }

    private void addSecondaryOutputWidgets(WidgetHolder widgets) {
        for (int i = 0; i < otherStacks.size(); i++) {
            PositionedStack stack = otherStacks.get(i);
            if (stack != null && stack.item != null) {
                int index = i;
                widgets.add(new NemiSlotWidget(() -> safeStack(neiHandler.getOtherStacks(recipeIndex), index), neiHandler, recipeIndex, stack.relx - 1, stack.rely - 1).drawBack(false).recipeContext(this));
            }
        }
    }

	private static void registerHandler(TemplateRecipeHandler handler) {
		synchronized (HANDLERS) {
			HANDLERS.add(handler);
		}
	}

	public static void tickHandlers() {
		synchronized (HANDLERS) {
			for (TemplateRecipeHandler handler : HANDLERS) {
				try {
					handler.onUpdate();
				} catch (Throwable ignored) {
				}
			}
		}
	}
}
