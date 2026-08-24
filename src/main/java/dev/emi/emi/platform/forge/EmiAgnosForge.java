package dev.emi.emi.platform.forge;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.gtnewhorizon.gtnhlib.client.model.baked.BakedModel;
import cpw.mods.fml.common.discovery.ASMDataTable;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemPotion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.text.WordUtils;

import com.google.common.collect.Lists;

import com.rewindmc.retroemi.RetroEMI;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import com.gtnewhorizon.gtnhlib.client.model.loading.ModelRegistry;
import com.gtnewhorizon.gtnhlib.client.model.loading.ResourceLoc;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import shim.com.mojang.blaze3d.systems.RenderSystem;
import shim.net.minecraft.client.gui.tooltip.TooltipComponent;
import shim.net.minecraft.client.util.ITooltipFlag;
import shim.net.minecraft.client.util.math.MatrixStack;
import shim.net.minecraft.registry.tag.ItemKey;
import shim.net.minecraft.text.Text;
import shim.net.minecraft.util.Formatting;

public class EmiAgnosForge extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosForge();
	}
	public static ASMDataTable asmDataTable;

	@Override
	protected boolean isForgeAgnos() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<? extends ModContainer> container = Optional.ofNullable(Loader.instance().getIndexedModList().get(namespace));
		if (container.isPresent()) {
			return container.get().getName();
		}
		container = Optional.ofNullable(Loader.instance().getIndexedModList().get(namespace.replace('_', '-')));
		if (container.isPresent()) {
			return container.get().getName();
		}
		return WordUtils.capitalizeFully(namespace.replace('_', ' '));
	}

	@Override
	protected Path getConfigDirectoryAgnos() {
		return Loader.instance().getConfigDir().toPath();
	}

	@Override
	protected boolean isDevelopmentEnvironmentAgnos() {
		return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}

	@Override
	protected boolean isModLoadedAgnos(String id) {
		return Loader.isModLoaded(id);
	}

	@Override
	protected List<String> getAllModNamesAgnos() {
		return Loader.instance().getActiveModList().stream().map(m -> m.getName()).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	protected List<String> getModsWithPluginsAgnos() {
		List<String> mods = Lists.newArrayList();
		if (asmDataTable != null) {
			for (ASMDataTable.ASMData asm : asmDataTable.getAll(EmiEntrypoint.class.getName())) {
				try {
					mods.add(asm.getCandidate().getContainedMods().get(0).getModId());
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return mods;
	}

	@Override
	protected List<EmiPluginContainer> getPluginsAgnos() {
		List<EmiPluginContainer> containers = Lists.newArrayList();
		if (asmDataTable != null) {
			for (ASMDataTable.ASMData asm : asmDataTable.getAll(EmiEntrypoint.class.getName())) {
				try {
					Class<?> clazz = Class.forName(asm.getClassName());
					if (EmiPlugin.class.isAssignableFrom(clazz)) {
						Class<? extends EmiPlugin> pluginClass = clazz.asSubclass(EmiPlugin.class);
						EmiPlugin plugin = pluginClass.getConstructor().newInstance();
						String id = asm.getCandidate().getContainedMods().get(0).getModId();
						containers.add(new EmiPluginContainer(plugin, id));
					} else {
						EmiLog.error("EmiEntrypoint " + asm.getClassName() + " does not implement EmiPlugin");
					}
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return containers;
	}

	@Override
	protected void addBrewingRecipesAgnos(EmiRegistry registry) {
		TileEntityBrewingStand tebs = new TileEntityBrewingStand();
		List<Item> ingredients = RetroEMI.getAllItems().stream().filter(i -> i != null && i.isPotionIngredient(new ItemStack(i))).collect(Collectors.toList());
		Set<Integer> seenPotions = new HashSet<>();
		IntList queue = new IntArrayList();
		queue.add(0);
		seenPotions.add(0);
		Map<Integer, Map<Item, Integer>> recipes = new HashMap<>();
		while (!queue.isEmpty()) {
			int potion = queue.removeInt(0);
			for (Item ing : ingredients) {
				try {
					int result = tebs.func_145936_c(potion, new ItemStack(ing));
					if (potion == result) continue;
					List<PotionEffect> inputEffects = Items.potionitem.getEffects(potion);
					List<PotionEffect> resultEffects = Items.potionitem.getEffects(result);
					boolean validBrew = (potion <= 0 || inputEffects != resultEffects) && (inputEffects == null || !inputEffects.equals(resultEffects) && resultEffects != null);
					boolean becomesSplash = !ItemPotion.isSplash(potion) && ItemPotion.isSplash(result);
					if (validBrew || becomesSplash) {
						recipes.computeIfAbsent(potion, k -> new HashMap<>()).put(ing, result);
						if (seenPotions.add(result)) queue.add(result);
					}
				} catch (Exception e) {
					EmiLog.error("Error discovering brewing recipe for potion " + potion, e);
				}
			}
		}
		String tid = EmiUtil.subId(Items.potionitem);
		for (Map.Entry<Integer, Map<Item, Integer>> entry : recipes.entrySet()) {
			int inputMeta = entry.getKey();
			for (Map.Entry<Item, Integer> recipeEntry : entry.getValue().entrySet()) {
				Item ing = recipeEntry.getKey();
				int resultMeta = recipeEntry.getValue();
				try {
					ResourceLocation id = EmiPort.id("emi", "/brewing/" + tid
						+ "/" + EmiUtil.subId(ing)
						+ "/" + tid + "_" + inputMeta
						+ "/" + tid + "_" + resultMeta);
					registry.addRecipe(new EmiBrewingRecipe(
						EmiStack.of(new ItemStack(Items.potionitem, 1, inputMeta)),
						EmiStack.of(new ItemStack(ing)),
						EmiStack.of(new ItemStack(Items.potionitem, 1, resultMeta)),
						id));
				} catch (Exception e) {
					EmiLog.error("Error registering brewing recipe", e);
				}
			}
		}
		Set<EmiStack> keptPotions = Collections.newSetFromMap(new IdentityHashMap<>());
		registry.removeEmiStacks(es -> {
			ItemStack is = es.getItemStack();
			if (is == null || is.getItem() != Items.potionitem) return false;
			return !keptPotions.contains(es);
		});
		List<EmiStack> sorted = seenPotions.stream()
			.map(m -> EmiStack.of(new ItemStack(Items.potionitem, 1, m)))
			.collect(Collectors.toList());
		for (EmiStack potion : sorted) {
			keptPotions.add(potion);
			registry.addEmiStack(potion);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<String> getAllModAuthorsAgnos() {
		return Loader.instance().getActiveModList().stream()
				.flatMap(container -> container.getMetadata().authorList.stream())
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());
	}

	@Override
	protected List<TooltipComponent> getItemTooltipAgnos(ItemStack stack) {
		return RetroEMI.getItemToolTip(stack, ITooltipFlag.TooltipFlags.ADVANCED).stream().map(TooltipComponent::of)
				.collect(java.util.stream.Collectors.toList());
	}

	@Override
	protected Text getFluidNameAgnos(Fluid fluid, NBTTagCompound nbt) {
		return Text.literal(new FluidStack(fluid, 1000, nbt).getLocalizedName());
	}

	@Override
	protected List<Text> getFluidTooltipAgnos(Fluid fluid, NBTTagCompound componentChanges) {
		List<Text> tooltip = Lists.newArrayList();
		tooltip.add(getFluidName(fluid, componentChanges));
		Minecraft client = Minecraft.getMinecraft();
		if (client.gameSettings.advancedItemTooltips) {
			tooltip.add(EmiPort.literal(fluid.getName()).formatted(Formatting.DARK_GRAY));
		}
		return tooltip;
	}

	@Override
	protected boolean isFloatyFluidAgnos(FluidEmiStack stack) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class), 1000, stack.getNbt());
		return fs.getFluid().getDensity() <= 0;
	}

	@Override
	protected void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class), 1000, stack.getNbt());
		Fluid ext = fs.getFluid();
		ResourceLocation texture = EmiPort.id(ext.getStillIcon().getIconName());
		if (texture == null) {
			return;
		}
		int color = ext.getColor(fs);
		RenderSystem.setShaderTexture(0, TextureMap.locationBlocksTexture);
		IIcon sprite = ext.getIcon();
		EmiRenderHelper.drawTintedSprite(matrices, sprite, color, x, y, xOff, yOff, width, height);
	}

	@Override
	protected EmiStack createFluidStackAgnos(Object object) {
		if (object instanceof FluidStack f) {
			return EmiStack.of(f.getFluid(), f.tag, f.amount);
		}
		return EmiStack.EMPTY;
	}

	@Override
	protected boolean canBatchAgnos(ItemStack stack) {
//		Minecraft client = Minecraft.getMinecraft();
//		RenderItem ir = client.getRenderItem();
//		IBakedModel model = ir.getItemModelWithOverrides(stack, client.world, null);
//		return model != null && model.getClass() == BakedItemModel.class;
		return false;
	}

	@Override
	protected Map<ItemKey, Integer> getFuelMapAgnos() {
		Map<ItemKey, Integer> fuelMap = new HashMap<>();
		for (Item item : RetroEMI.getAllItems()) {
			List<ItemStack> stacks = new ArrayList<>();
			item.getSubItems(item, CreativeTabs.tabAllSearch, stacks);
			for (ItemStack stack : stacks) {
				int time = TileEntityFurnace.getItemBurnTime(stack);
				if (time > 0) {
					fuelMap.put(ItemKey.of(stack), time);
				}
			}
		}
		return fuelMap;
	}

	@Override
	protected BakedModel getBakedTagModelAgnos(ResourceLocation id) {
		try {
			return ModelRegistry.getJSONModel(ResourceLoc.ModelLoc.fromStr(id.toString())).bake();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment) {
		ItemStack enchantedBook = new ItemStack(Items.enchanted_book);
		EnchantmentHelper.setEnchantments(Collections.singletonMap(enchantment.effectId, enchantment.getMaxLevel()), enchantedBook);
		return stack.getItem().isBookEnchantable(stack, enchantedBook);
	}
}
