package dev.emi.emi;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.rewindmc.retroemi.EmiResourceManager;
import dev.emi.emi.api.stack.Comparison;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import shim.net.minecraft.client.gui.widget.ButtonWidget;
import shim.net.minecraft.client.gui.widget.TextFieldWidget;
import shim.net.minecraft.text.MutableText;
import shim.net.minecraft.text.OrderedText;
import shim.net.minecraft.text.Style;
import shim.net.minecraft.text.Text;
import shim.net.minecraft.util.Formatting;

/**
 * Multiversion quarantine, to avoid excessive git pain
 */
public final class EmiPort {

	public static MutableText literal(String s) {
		return Text.literal(s);
	}

	public static MutableText literal(String s, Formatting formatting) {
		return Text.literal(s).formatted(formatting);
	}

	public static MutableText literal(String s, Formatting... formatting) {
		return Text.literal(s).formatted(formatting);
	}

	public static MutableText literal(String s, Style style) {
		return Text.literal(s).setStyle(style);
	}

	public static MutableText translatable(String s) {
		return Text.translatable(s);
	}

	public static MutableText translatable(String s, Formatting formatting) {
		return Text.translatable(s).formatted(formatting);
	}

	public static MutableText translatable(String s, Object... objects) {
		return Text.translatable(s, objects);
	}

	public static MutableText append(MutableText text, Text appended) {
		return text.append(appended);
	}

	public static OrderedText ordered(Text text) {
		return text.asOrderedText();
	}

	public static Collection<ResourceLocation> findResources(IResourceManager manager, String prefix, Predicate<String> pred) {
		return EmiResourceManager.instance.findResources(manager, prefix, i -> pred.test(i.toString())).keySet();
	}

	public static InputStream getInputStream(IResource resource) {
		try {
			return resource.getInputStream();
		} catch (Exception e) {
			return null;
		}
	}
	public static NBTTagList addRandomBanner(NBTTagList patterns, Random random) {
		BannerPattern pattern = BannerPattern.values()[random.nextInt(BannerPattern.values().length)];
		EnumDyeColor color = EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)];
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Pattern", pattern.getHashname());
		tag.setInteger("Color", color.getDyeDamage());
		patterns.appendTag(tag);
		return patterns;
	}

//	public static boolean canTallFlowerDuplicate(BlockDoublePlant tallFlowerBlock) {
//		try {
//			return tallFlowerBlock.canGrow(null, null, null, true);
//		} catch(Exception e) {
//			return false;
//		}
//	}

//	public static void setShader(VertexBuffer buf, Matrix4f mat) {
//		buf.bind();
//		buf.draw(mat, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
//	}

	public static List<BakedQuad> getQuads(IBakedModel model) {
		return model.getQuads(null, null, 0L);
	}

	public static void draw(BufferBuilder bufferBuilder) {
		Tessellator.getInstance().draw();
	}

	public static int getGuiScale(Minecraft client) {
		return new ScaledResolution(client).getScaleFactor();
	}

	public static void setPositionTexShader() {
		GlStateManager.enableTexture2D();
	}

	public static void setPositionColorTexShader() {
		GlStateManager.enableTexture2D();
	}

	public static RegistryNamespaced<ResourceLocation, Item> getItemRegistry() {
		return Item.REGISTRY;
	}

	public static RegistryNamespaced<ResourceLocation, Block> getBlockRegistry() {
		return Block.REGISTRY;
	}

	public static Map<String, Fluid> getFluidRegistry() {
		return FluidRegistry.getRegisteredFluids();
	}

	public static RegistryNamespaced<ResourceLocation, PotionType> getPotionRegistry() {
		return PotionType.REGISTRY;
	}

	@SuppressWarnings("unchecked")
	public static RegistryNamespaced<ResourceLocation, Enchantment> getEnchantmentRegistry() {
		return Enchantment.REGISTRY;
	}

	public static ButtonWidget newButton(int x, int y, int w, int h, Text name, ButtonWidget.PressAction action) {
		return ButtonWidget.builder(name, action).position(x, y).size(w, h).build();
	}

	public static ItemStack getOutput(IRecipe recipe) {
		return recipe.getRecipeOutput();
	}

	public static void focus(TextFieldWidget widget, boolean focused) {
//		// Also ensure a current focus-element in the screen is cleared if it changes
//		Minecraft client = Minecraft.getMinecraft();
//		if (client != null && client.currentScreen != null) {
//			var currentFocus = client.currentScreen.getFocused();
//			if (!focused && currentFocus == widget || focused && currentFocus != widget) {
//				client.currentScreen.setFocused(null);
//			}
//		}
		widget.setFocused(focused);
	}

	public static Stream<Item> getDisabledItems() {
		return getItemRegistry().getKeys().stream().map(EmiPort.getItemRegistry()::getObject);
	}

	public static ResourceLocation getId(IRecipe recipe) {
		return ForgeRegistries.RECIPES.getKey(recipe);
	}

	public static @Nullable IRecipe getRecipe(ResourceLocation id) {
		Minecraft client = Minecraft.getMinecraft();
		if (client.world != null && id != null) {
			return ForgeRegistries.RECIPES.getValue(id);
		}
		return null;
	}

	public static Comparison compareStrict() {
		return Comparison.compareComponents();
	}

	public static ItemStack setPotion(ItemStack stack, PotionType potion) {
		return PotionUtils.addPotionToItemStack(stack, potion);
	}

	public static NBTTagCompound emptyExtraData() {
		return null;
	}

	public static ResourceLocation id(String id) {
		if (id.contains(":")) {
			String[] parts = id.split(":", 2);
			String mod = parts[0];//Avoid mods being forced to lowercase and not being able to get them
			String name = parts[1];
			return new ResourceLocation(mod, name);
		}
		return new ResourceLocation(id);
	}

	public static ResourceLocation id(String namespace, String path) {
		return new ResourceLocation(namespace, path);
	}

	public static void applyModelViewMatrix() {
		RenderSystem.applyModelViewMatrix();
	}
}
