package shim.net.minecraft.nbt;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;
import java.util.Base64;

public class StringNbtReader {

	public static NBTTagCompound parse(String nbt) throws IOException {
		if (nbt.isEmpty() || nbt.equals("{}")) return new NBTTagCompound();
		if (nbt.startsWith("{{binnbt:") && nbt.endsWith("}}")) {
            return CompressedStreamTools.func_152457_a(Base64.getDecoder().decode(nbt.substring(9, nbt.length()-2)), new NBTSizeTracker(2097152L));
        }
		throw new IllegalArgumentException(nbt + " doesn't look like binnbt (retroEMI does not implement Mojangson/SNBT)");
	}

	public static String encode(NBTTagCompound nbt) throws IOException {
        return "{{binnbt:" + Base64.getEncoder().encodeToString(CompressedStreamTools.compress(nbt)) + "}}";
    }

}
