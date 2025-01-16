package com.diskree.advancementsexplorer.injection.mixin;

import com.diskree.advancementsexplorer.AdvancementsExplorerMod;
import com.diskree.advancementsexplorer.injection.extension.AdvancementsScreenExtension;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {

    @Unique
    private static final Identifier ADVANCEMENTS_SEARCH_TAB_ROOT_ADVANCEMENT_ID =
        Identifier.of("advancementssearch", "advancementssearch" + "/root");

    @Shadow
    @Final
    private AdvancementsScreen screen;

    @Shadow
    @Final
    private PlacedAdvancement root;

    @Inject(
        method = "drawWidgetTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;drawTooltip(Lnet/minecraft/client/gui/DrawContext;IIFII)V",
            shift = At.Shift.AFTER
        )
    )
    public void saveFocusedAdvancementWidget(
        DrawContext context,
        int mouseX,
        int mouseY,
        int x,
        int y,
        CallbackInfo ci,
        @Local(ordinal = 0) AdvancementWidget advancementWidget
    ) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension &&
            !root.getAdvancementEntry().id().equals(ADVANCEMENTS_SEARCH_TAB_ROOT_ADVANCEMENT_ID)
        ) {
            AdvancementEntry advancementEntry = advancementWidget.advancement.getAdvancementEntry();
            if (AdvancementsExplorerMod.isClickableAdvancement(advancementEntry)) {
                advancementsScreenExtension.advancementsexplorer$setFocusedAdvancement(
                    advancementEntry.id(),
                    advancementWidget
                );
            } else {
                advancementsScreenExtension.advancementsexplorer$setFocusedAdvancement(null, null);
            }
        }
    }

    @Inject(
        method = "drawWidgetTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/util/math/MatrixStack;pop()V",
            shift = At.Shift.AFTER
        )
    )
    public void resetFocusedAdvancementWidget(
        DrawContext context,
        int mouseX,
        int mouseY,
        int x,
        int y,
        CallbackInfo ci,
        @Local(ordinal = 0) boolean shouldShowTooltip
    ) {
        if (!shouldShowTooltip && screen instanceof AdvancementsScreenExtension advancementsScreenExtension &&
            !root.getAdvancementEntry().id().equals(ADVANCEMENTS_SEARCH_TAB_ROOT_ADVANCEMENT_ID)
        ) {
            advancementsScreenExtension.advancementsexplorer$setFocusedAdvancement(null, null);
        }
    }
}
