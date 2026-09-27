package com.rewindmc.retroemi;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import dev.emi.emi.EmiPort;
import shim.net.minecraft.item.ItemStacks;

import java.util.ArrayList;
import java.util.List;

public class RetroEMICommonUtils {
    private static final List<Runnable> tickQueue = new ArrayList<>();

    public static void executeOnMainThread(Runnable r) {
        synchronized (tickQueue) {
            tickQueue.add(r);
        }
    }

    public static void tick() {
        Runnable[] queue;
        synchronized (tickQueue) {
            queue = tickQueue.toArray(new Runnable[tickQueue.size()]);
            tickQueue.clear();
        }
        for (Runnable r : queue) {
            r.run();
        }
    }

    public static List<String> wrapLines(String str, int cols) {
        ArrayList<String> li = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();
        for (String line : str.split("\n")) {
            int w = -1;
            for (String word : line.split(" ")) {
                if (w + 1 + word.length() > cols) {
                    li.add(buf.toString());
                    buf.setLength(0);
                    w = 0;
                } else {
                    if (w != -1) buf.append(" ");
                    w++;
                }
                while (word.length() > cols) {
                    li.add(word.substring(0, cols));
                    word = word.substring(cols);
                }
                buf.append(word);
                w += word.length();
            }
            if (buf.length() > 0) {
                li.add(buf.toString());
            }
            buf.setLength(0);
        }
        return li;
    }

    public static String join(List<String> strs, String delim) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : strs) {
            if (first) {
                first = false;
            } else {
                sb.append(delim);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static void offerOrDrop(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.dropPlayerItemWithRandomChoice(stack, false);
        }
    }

    public static boolean canCombine(ItemStack a, ItemStack b) {
        return a == null || b == null ? a == b : a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static String replaceCharAt(String s, int index, char c) {
        return s.substring(0, index) + c + s.substring(index + 1);
    }

    private static @Nullable String getIdInner(ItemStack stack) {
        if (ItemStacks.isEmpty(stack)) {
            return null;
        }
        Item item = stack.getItem();
        if (item instanceof ItemBlock ib) {
            return EmiPort.getBlockRegistry().getNameForObject(ib.field_150939_a);
        } else {
            return EmiPort.getItemRegistry().getNameForObject(item);
        }
    }

    public static @Nullable String getId(ItemStack stack) {
        String s = getIdInner(stack);
        if (s != null && s.contains(":")) {
            String[] parts = s.split(":");
            return parts[1];
        }
        return null;
    }
}
