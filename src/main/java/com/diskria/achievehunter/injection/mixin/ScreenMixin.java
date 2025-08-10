package com.diskria.achievehunter.injection.mixin;

import com.diskria.achievehunter.injection.extension.AdvancementsScreenExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(
            method = "tick",
            at = @At(value = "HEAD")
    )
    private void tickBridge(CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            advancementsScreenExtension.achievehunter$tick();
        }
    }

    @Inject(
            method = "resize",
            at = @At(value = "HEAD")
    )
    private void resizeBridge(MinecraftClient client, int width, int height, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            advancementsScreenExtension.achievehunter$resize(client, width, height);
        }
    }
}
