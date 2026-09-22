package shim.net.minecraft.network.packet;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public interface CustomPayload extends IMessage {
	ResourceLocation getId();
}
