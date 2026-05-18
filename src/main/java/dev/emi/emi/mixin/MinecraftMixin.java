//package dev.emi.emi.mixin;
//
//import com.google.common.util.concurrent.Futures;
//import com.google.common.util.concurrent.ListenableFuture;
//import com.google.common.util.concurrent.MoreExecutors;
//
//import dev.emi.emi.platform.EmiClient;
//import dev.emi.emi.platform.forge.EmiClientForge;
//import dev.emi.emi.runtime.EmiLog;
//import dev.emi.emi.runtime.EmiReloadManager;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.WorldClient;
//import net.minecraftforge.fml.common.registry.ForgeRegistries;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//import java.util.concurrent.Executors;
//
//@Mixin(Minecraft.class)
//public abstract class MinecraftMixin {
//	@Inject(at = @At("RETURN"), method = "scheduleResourcesRefresh", cancellable = true)
//	public void reloadResources(CallbackInfoReturnable<ListenableFuture<Object>> info) {
//		ListenableFuture<Object> future = Futures.transform(info.getReturnValue(), result -> {
//			Minecraft client = Minecraft.getMinecraft();
//			if (client.world != null && ForgeRegistries.RECIPES != null) {
//				EmiReloadManager.reload();
//			}
//			return result;
//		}, MoreExecutors.directExecutor());
//		info.setReturnValue(future);
//	}
//}
