package dev.emi.emi.nemi.runtime;

import java.awt.Point;
import java.util.ArrayList;

import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class NemiGuiRecipe extends GuiRecipe<IRecipeHandler> {
	private static final int DEFAULT_X_SIZE = 176;
	private static final int DEFAULT_Y_SIZE = 166;

	private static NemiGuiRecipe instance;
	private int originX;
	private int originY;

	private NemiGuiRecipe() {
		super(null);
		this.mc = Minecraft.getMinecraft();
	}

	public static NemiGuiRecipe instance() {
		if (instance == null) {
			try {
				instance = new NemiGuiRecipe();
			} catch (Throwable t) {
				return null;
			}
		}
		return instance;
	}

	public void setRecipeOrigin(int x, int y) {
		GuiScreen screen = this.mc.currentScreen;
		this.width = screen.width;
		this.height = screen.height;
		this.guiLeft = (this.width - DEFAULT_X_SIZE) / 2;
		this.guiTop = (this.height - DEFAULT_Y_SIZE) / 2;
		this.originX = x;
		this.originY = y;
	}

	@Override
	public Point getRecipePosition(int recipe) {
		return new Point(originX - guiLeft, originY - guiTop);
	}

	@Override
	public ArrayList<IRecipeHandler> getCurrentRecipeHandlers() {
		return new ArrayList<>();
	}
}
