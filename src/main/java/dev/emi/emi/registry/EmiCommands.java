package dev.emi.emi.registry;

import java.util.List;

import dev.emi.emi.EmiPort;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiNetwork;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public class EmiCommands extends CommandBase {
	public static final byte VIEW_RECIPE = 0x01;
	public static final byte VIEW_TREE = 0x02;
	public static final byte TREE_GOAL = 0x11;
	public static final byte TREE_RESOLUTION = 0x12;

	@Override
	public String getCommandName() {
		return "emi";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.emi.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] userInputStrings) {
        if (sender instanceof EntityPlayerMP player) {
            if (userInputStrings.length >= 2) {
                if (userInputStrings[0].equals("view")) {
                    if (userInputStrings[1].equals("recipe")) {
                        if (userInputStrings[2].isEmpty()) {
                            throw new WrongUsageException("commands.emi.id");
                        } else {
                            ResourceLocation id = EmiPort.id(userInputStrings[2]);
                            send(player, VIEW_RECIPE, id);
                        }
                    } else if (userInputStrings[1].equals("tree")) {
                        send(player, VIEW_TREE, null);
                    }
                } else if (userInputStrings[0].equals("tree")) {
                    if (userInputStrings[1].equals("goal")) {
                        if (userInputStrings[2].isEmpty()) {
                            throw new WrongUsageException("commands.emi.id");
                        } else {
                            ResourceLocation id = EmiPort.id(userInputStrings[2]);
                            send(player, TREE_GOAL, id);
                        }
                    } else if (userInputStrings[1].equals("resolution")) {
                        if (userInputStrings[2].isEmpty()) {
                            throw new WrongUsageException("commands.emi.id");
                        } else {
                            ResourceLocation id = EmiPort.id(userInputStrings[2]);
                            send(player, TREE_RESOLUTION, id);
                        }
                    }
                }
            }
        }
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] userInputStrings) {
		if (userInputStrings.length == 1) {
			return getListOfStringsMatchingLastWord(userInputStrings, "view", "tree");
		}
		if (userInputStrings.length == 2) {
			if (userInputStrings[0].equals("view")) {
				return getListOfStringsMatchingLastWord(userInputStrings, "recipe", "tree");
			} else if (userInputStrings[0].equals("tree")) {
				return getListOfStringsMatchingLastWord(userInputStrings, "goal", "resolution");
			}
		}
		if (userInputStrings.length == 3) {
			if (!userInputStrings[1].equals("tree")) {
				List<EmiRecipe> recipeList = EmiApi.getRecipeManager().getRecipes();
				return getListOfStringsMatchingLastWord(userInputStrings, "");
			}
		}
		return null;
	}

	private static void send(EntityPlayerMP player, byte type, @Nullable ResourceLocation id) {
		EmiNetwork.sendToClient(player, new CommandS2CPacket(type, id));
	}
}
