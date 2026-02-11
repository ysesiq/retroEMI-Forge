//package net.minecraft.tag;
//
//import com.rewindmc.retroemi.Prototype;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.ResourceLocation;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class WildcardItemTag implements TagKey<Prototype> {
//
//	private final Item item;
//	private final List<Prototype> subtypes;
//	public WildcardItemTag(Item item) {
//		this.item = item;
//		ArrayList<ItemStack> li = new ArrayList<ItemStack>();
//		item.getSubItems(item, item.getCreativeTab(), li);
//		if (li.isEmpty()) {
//			li.add(new ItemStack(item));
//		}
//		subtypes = li.stream()
//				.map(Prototype::of)
//				.collect(Collectors.toList());
//	}
//
//	@Override
//	public ResourceLocation id() {
//		return new ResourceLocation("wildcard", item.getItemStackDisplayName(new ItemStack(item)) + "/" + Item.getIdFromItem(item));
//	}
//
//	@Override
//	public List<Prototype> get() {
//		return subtypes;
//	}
//
//	@Override
//	public String getFlavor() {
//		return "wildcard";
//	}
//
//}
