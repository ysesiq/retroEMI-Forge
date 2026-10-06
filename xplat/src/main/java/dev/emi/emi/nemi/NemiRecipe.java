package dev.emi.emi.nemi;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

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
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.nemi.runtime.NemiGuiRecipe;
import dev.emi.emi.nemi.widget.NemiSlotWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import shim.net.minecraft.client.gui.DrawContext;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.text.Text;

public class NemiRecipe implements EmiRecipe {
    public EmiRecipeCategory recipeCategory;
    public ResourceLocation id;
    public TemplateRecipeHandler handler;
    public int recipeIndex;
    public List<EmiIngredient> inputs;
    public List<EmiStack> outputs;

    private static final Set<TemplateRecipeHandler> HANDLERS = Collections.newSetFromMap(new WeakHashMap<>());

    private final List<PositionedStack> ingredientStacks;
    private final PositionedStack resultStack;
    private final List<PositionedStack> otherStacks;

    public NemiRecipe(EmiRecipeCategory recipeCategory, TemplateRecipeHandler handler, int recipeIndex, ResourceLocation id) {
        this.recipeCategory = recipeCategory;
        this.handler = handler;
        this.recipeIndex = recipeIndex;
        this.id = id;

        this.ingredientStacks = safeList(handler.getIngredientStacks(recipeIndex));
        this.resultStack = handler.getResultStack(recipeIndex);
        this.otherStacks = safeList(handler.getOtherStacks(recipeIndex));

        this.inputs = parseInputs();
        this.outputs = parseOutputs();
        registerHandler(handler);
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
            parsedInputs.add(NemiUtil.parseIngredient(stack));
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

    public RecipeId getNeiRecipeId() {
        return RecipeId.of(handler, recipeIndex);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return recipeCategory;
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
		return Math.max(HandlerInfo.DEFAULT_WIDTH, GuiRecipeTab.getHandlerInfo(handler).getWidth());
	}

	@Override
	public int getDisplayHeight() {
		HandlerInfo info = GuiRecipeTab.getHandlerInfo(handler);
		int recipeHeight = 0;
//		try {
			recipeHeight = handler.getRecipeHeight(recipeIndex);
//		} catch (Throwable ignored) {
//			recipeHeight = info.getHeight();
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
        widgets.add(new NemiWidget(0, 0, getDisplayWidth(), getDisplayHeight()));
        addInputWidgets(widgets);
        addMainOutputWidget(widgets);
        addSecondaryOutputWidgets(widgets);
    }

    private void addInputWidgets(WidgetHolder widgets) {
        List<PositionedStack> stacks = safeList(handler.getIngredientStacks(recipeIndex));
        for (int i = 0; i < stacks.size(); i++) {
            PositionedStack stack = stacks.get(i);
            int index = i;
            widgets.add(new NemiSlotWidget(() -> safeStack(handler.getIngredientStacks(recipeIndex), index), handler, recipeIndex, stack.relx - 1, stack.rely - 1).drawBack(false));
        }
    }

    private void addMainOutputWidget(WidgetHolder widgets) {
        if (resultStack != null && resultStack.item != null) {
            widgets.add(new NemiSlotWidget(() -> handler.getResultStack(recipeIndex), handler, recipeIndex, resultStack.relx - 1, resultStack.rely - 1).drawBack(false).recipeContext(this));
        }
    }

    private void addSecondaryOutputWidgets(WidgetHolder widgets) {
        for (int i = 0; i < otherStacks.size(); i++) {
            PositionedStack stack = otherStacks.get(i);
            if (stack != null && stack.item != null) {
                int index = i;
                widgets.add(new NemiSlotWidget(() -> safeStack(handler.getOtherStacks(recipeIndex), index), handler, recipeIndex, stack.relx - 1, stack.rely - 1).drawBack(false).recipeContext(this));
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

    public class NemiWidget extends Widget {

        private final Bounds bounds;
        private final int x, y;

        public NemiWidget(int x, int y, int w, int h) {
            this.bounds = new Bounds(x, y, w, h);
            this.x = x;
            this.y = y;
        }

        @Override
        public Bounds getBounds() {
            return bounds;
        }

        @Override
        public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
            EmiDrawContext context = EmiDrawContext.instance();
            context.push();
            context.translate(x, y);
            context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            context.enableBlend();
            try {
                handler.drawBackground(recipeIndex);
                handler.drawForeground(recipeIndex);
            } catch (Exception e) {
                EmiLog.error("Error drawing NEI background for recipe " + id, e);
            } finally {
                context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                context.pop();
            }
        }

        @Override
        public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
            try {
                NemiGuiRecipe gui = NemiGuiRecipe.instance();
                if (gui == null) {
                    return new ArrayList<>();
                }
                Point global = GuiDraw.getMousePosition();
                gui.setRecipeOrigin(global.x - mouseX, global.y - mouseY);
                List<String> tooltip = handler.handleTooltip(gui, new ArrayList<>(), recipeIndex);
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
        }
    }
}
