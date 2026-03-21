package dev.emi.emi.network;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.registry.EmiCommands;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class CommandS2CPacket implements EmiPacket {
	private final byte type;
	private final ResourceLocation id;

	public CommandS2CPacket(byte type, ResourceLocation id) {
		this.type = type;
		this.id = id;
	}

	public CommandS2CPacket(PacketBuffer buf) {
		type = buf.readByte();
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			String path = buf.readString(255);
			String domain = buf.readString(255);
			id = EmiPort.id(domain, path);
		} else {
			id = null;
		}
	}

	@Override
	public void write(PacketBuffer buf) {
		buf.writeByte(type);
		if (type == EmiCommands.VIEW_RECIPE || type == EmiCommands.TREE_GOAL || type == EmiCommands.TREE_RESOLUTION) {
			buf.writeString(id.getPath());
			buf.writeString(id.getNamespace());
		}
	}

	@Override
	public void apply(EntityPlayer player) {
		if (type == EmiCommands.VIEW_RECIPE) {
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null) {
				EmiApi.displayRecipe(recipe);
			}
		} else if (type == EmiCommands.VIEW_TREE) {
			EmiApi.viewRecipeTree();
		} else if (type == EmiCommands.TREE_GOAL) {
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null) {
				BoM.setGoal(recipe);
			}
		} else if (type == EmiCommands.TREE_RESOLUTION) {
			EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
			if (recipe != null && BoM.tree != null) {
				for (EmiStack stack : recipe.getOutputs()) {
					BoM.tree.addResolution(stack, recipe);
				}
			}
		}
	}

	@Override
	public ResourceLocation getId() {
		return EmiNetwork.COMMAND;
	}
}
