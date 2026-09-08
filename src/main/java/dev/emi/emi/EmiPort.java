package dev.emi.emi;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.rewindmc.retroemi.client.EmiResourceManager;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import dev.emi.emi.api.stack.Comparison;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;
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
//	public static NBTTagList addRandomBanner(NBTTagList patterns, Random random) {
//		BannerPattern pattern = BannerPattern.values()[random.nextInt(BannerPattern.values().length)];
//		EnumDyeColor color = EnumDyeColor.values()[random.nextInt(EnumDyeColor.values().length)];
//		NBTTagCompound tag = new NBTTagCompound();
//		tag.setString("Pattern", pattern.getHashname());
//		tag.setInteger("Color", color.getDyeDamage());
//		patterns.appendTag(tag);
//		return patterns;
//	}

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

//	public static List<BakedQuad> getQuads(IBakedModel model) {
//		return model.getQuads(null, null, 0L);
//	}

	public static void draw(Tessellator bufferBuilder) {
		Tessellator.instance.draw();
	}

	public static int getGuiScale(Minecraft client) {
		return new ScaledResolution(client, client.displayWidth, client.displayHeight).getScaleFactor();
	}

	public static void setPositionTexShader() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void setPositionColorTexShader() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static FMLControlledNamespacedRegistry<Item> getItemRegistry() {
		return GameData.getItemRegistry();
	}

	public static FMLControlledNamespacedRegistry<Block> getBlockRegistry() {
		return GameData.getBlockRegistry();
	}

	public static Map<String, Fluid> getFluidRegistry() {
		return FluidRegistry.getRegisteredFluids();
	}

	public static Map<String, Fluid>  getPotionRegistry() {
		return FluidRegistry.getRegisteredFluids();
	}

	@SuppressWarnings("unchecked")
	public static Enchantment[] getEnchantmentRegistry() {
		return Enchantment.enchantmentsList;
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

	public static Comparison compareStrict() {
		return Comparison.compareComponents();
	}

//	public static ItemStack setPotion(ItemStack stack, PotionType potion) {
//		return PotionUtils.addPotionToItemStack(stack, potion);
//	}

	public static NBTTagCompound emptyExtraData() {
		return null;
	}

	public static ResourceLocation id(String id) {
        if (id == null) {
            return new ResourceLocation("unknown", "unknown");
        }
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

	public static void playClickSound() {
		Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(EmiPort.id("gui.button.press"), 1.0f));
	}
}
