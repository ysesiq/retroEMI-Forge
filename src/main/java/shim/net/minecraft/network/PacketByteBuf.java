package shim.net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public interface PacketByteBuf {

	byte readByte();
	ItemStack readItemStack();
	String readString();
	int readInt();
	int readVarInt();
	boolean readBoolean();

	void writeByte(int b);
	void writeItemStack(ItemStack stack);
	void writeString(String str);
	void writeInt(int i);
	void writeVarInt(int i);
	void writeBoolean(boolean b);

	static PacketByteBuf out(DataOutputStream out) {
		return new PacketByteBuf() {

			@Override
			public void writeVarInt(int value) {
				while ((value & -128) != 0) {
					writeByte(value & 127 | 128);
					value >>>= 7;
				}
				writeByte(value);
			}

			@Override
			public void writeString(String str) {
				byte[] bys = str.getBytes(StandardCharsets.UTF_8);
				writeVarInt(bys.length);
				try { out.write(bys);
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override
			public void writeItemStack(ItemStack stack) {
                ByteBuf bytebuf = Unpooled.buffer();
                try { (new PacketBuffer(bytebuf)).writeItemStackToBuffer(stack);
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override
			public void writeInt(int i) {
				try { out.writeInt(i);
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override
			public void writeByte(int b) {
				try { out.writeByte(b);
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override
			public void writeBoolean(boolean b) {
				writeByte(b ? 1 : 0);
			}

			@Override public int readVarInt() { throw new UnsupportedOperationException(); }
			@Override public String readString() { throw new UnsupportedOperationException(); }
			@Override public ItemStack readItemStack() { throw new UnsupportedOperationException(); }
			@Override public int readInt() { throw new UnsupportedOperationException(); }
			@Override public byte readByte() { throw new UnsupportedOperationException(); }
			@Override public boolean readBoolean() { throw new UnsupportedOperationException(); }
		};
	}

	static PacketByteBuf in(DataInputStream in) {
		return new PacketByteBuf() {

			@Override
			public int readVarInt() {
				int i = 0;
				int j = 0;

				byte b;
				do {
					b = readByte();
					i |= (b & 127) << j++ * 7;
					if (j > 5) throw new RuntimeException("VarInt too big");
				} while ((b & 128) == 128);

				return i;
			}

			@Override
			public String readString() {
				int len = readVarInt();
				byte[] bys = new byte[len];
				try { in.readFully(bys);
				} catch (IOException e) { throw new UncheckedIOException(e); }
				return new String(bys, StandardCharsets.UTF_8);
			}

			@Override
			public ItemStack readItemStack() {
                ByteBuf bytebuf = Unpooled.buffer();
                try { return (new PacketBuffer(bytebuf)).readItemStackFromBuffer();
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override
			public int readInt() {
				try { return in.readInt();
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override
			public byte readByte() {
				try { return in.readByte();
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override
			public boolean readBoolean() {
				try { return in.readByte() != 0;
				} catch (IOException e) { throw new UncheckedIOException(e); }
			}

			@Override public void writeVarInt(int value) { throw new UnsupportedOperationException(); }
			@Override public void writeString(String str) { throw new UnsupportedOperationException(); }
			@Override public void writeItemStack(ItemStack stack) { throw new UnsupportedOperationException(); }
			@Override public void writeInt(int i) { throw new UnsupportedOperationException(); }
			@Override public void writeByte(int b) { throw new UnsupportedOperationException(); }
			@Override public void writeBoolean(boolean b) { throw new UnsupportedOperationException(); }
		};
	}

}
