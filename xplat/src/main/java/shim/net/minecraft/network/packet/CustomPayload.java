package shim.net.minecraft.network.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraft.util.ResourceLocation;

public interface CustomPayload extends IMessage {
	ResourceLocation getId();
}
