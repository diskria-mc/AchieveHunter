package com.diskree.achievehunter.injection.mixin;

import com.diskree.achievehunter.injection.extension.AdvancementsScreenExtension;
import net.minecraft.client.gui.ParentElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ParentElement.class, priority = 500)
public interface ParentElementMixin {

    @Inject(
        method = "mouseReleased",
        at = @At(value = "HEAD")
    )
    private void onMouseReleasedInAdvancementsScreen(
        double mouseX,
        double mouseY,
        int button,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (this instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            advancementsScreenExtension.achievehunter$onMouseReleased(mouseX, mouseY, button);
        }
    }

    @Inject(
        method = "charTyped",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private void onCharTypedInAdvancementsScreen(char chr, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof AdvancementsScreenExtension advancementsScreenExtension &&
            advancementsScreenExtension.achievehunter$charTyped(chr, modifiers)
        ) {
            cir.setReturnValue(true);
        }
    }
}
