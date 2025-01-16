package com.diskree.advancementsexplorer.injection.mixin;

import com.diskree.advancementsexplorer.injection.extension.AdvancementsScreenExtension;
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
            advancementsScreenExtension.advancementsexplorer$tick();
        }
    }
}
