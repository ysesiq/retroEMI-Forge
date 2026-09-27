package dev.emi.emi.nemi;

import java.awt.Point;
import java.util.ArrayList;

import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.IRecipeHandler;
import net.minecraft.client.Minecraft;

public class NemiGuiRecipe extends GuiRecipe<IRecipeHandler> {
	private static NemiGuiRecipe instance;
	private int originX;
	private int originY;

	private NemiGuiRecipe() {
		super(null);
		this.mc = Minecraft.getMinecraft();
		this.guiLeft = 0;
		this.guiTop = 0;
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
		this.originX = x;
		this.originY = y;
	}

	@Override
	public Point getRecipePosition(int recipe) {
		return new Point(originX, originY);
	}

	@Override
	public ArrayList<IRecipeHandler> getCurrentRecipeHandlers() {
		return new ArrayList<>();
	}
}
