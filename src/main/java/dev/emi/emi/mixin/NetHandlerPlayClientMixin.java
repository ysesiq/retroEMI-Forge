package dev.emi.emi.mixin;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.network.NetHandlerPlayClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {

    @Inject(at = @At("RETURN"), method = "handleJoinGame")
    private void onGameJoin(CallbackInfo info) {
        EmiLog.info("Joining server, EMI waiting for data from server...");
    }
}
