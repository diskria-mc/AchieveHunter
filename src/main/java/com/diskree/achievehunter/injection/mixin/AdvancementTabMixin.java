package com.diskree.achievehunter.injection.mixin;

import com.diskree.achievehunter.injection.extension.AdvancementsScreenExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

import static net.minecraft.client.gui.screen.advancement.AdvancementsScreen.PAGE_HEIGHT;
import static net.minecraft.client.gui.screen.advancement.AdvancementsScreen.PAGE_WIDTH;

@Mixin(AdvancementTab.class)
public class AdvancementTabMixin {

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
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            advancementsScreenExtension.achievehunter$setFocusedAdvancement(advancementWidget);
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
        if (!shouldShowTooltip && screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            advancementsScreenExtension.achievehunter$setFocusedAdvancement(null);
        }
    }

    @ModifyConstant(
        method = "move",
        constant = @Constant(
            intValue = PAGE_WIDTH,
            ordinal = 0
        )
    )
    private int calculateMoveLimitByX(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeWidth();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "move",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 0
        )
    )
    private int calculateMoveLimitByY(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeHeight();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "move",
        constant = @Constant(
            intValue = PAGE_WIDTH,
            ordinal = 1
        )
    )
    private int calculateMoveMinimumX(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeWidth();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "move",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 1
        )
    )
    private int calculateMoveMinimumY(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeHeight();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "drawWidgetTooltip",
        constant = @Constant(
            intValue = PAGE_WIDTH,
            ordinal = 0
        )
    )
    private int calculateWidthForTooltipDim(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeWidth();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "drawWidgetTooltip",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 0
        )
    )
    private int calculateHeightForTooltipDim(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeHeight();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "drawWidgetTooltip",
        constant = @Constant(
            intValue = PAGE_WIDTH,
            ordinal = 1
        )
    )
    private int calculateWidthForWidgetHoverCheck(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeWidth();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "drawWidgetTooltip",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 1
        )
    )
    private int calculateHeightForWidgetHoverCheck(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeHeight();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = PAGE_WIDTH,
            ordinal = 0
        )
    )
    private int drawFullscreenBackgroundByWidth(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeWidth();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 0
        )
    )
    private int drawFullscreenBackgroundByHeight(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeHeight();
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = PAGE_WIDTH / 2,
            ordinal = 0
        )
    )
    private int calculateWidthForOriginX(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeWidth() / 2;
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = PAGE_HEIGHT / 2,
            ordinal = 0
        )
    )
    private int calculateHeightForOriginY(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getTreeHeight() / 2;
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = 15,
            ordinal = 0
        )
    )
    private int calculateWidthForBackgroundGridColumnsCount(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getWindowWidth() / 16 + 1;
        }
        return originalValue;
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = 8,
            ordinal = 0
        )
    )
    private int calculateHeightForBackgroundGridRowsCount(int originalValue) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            return advancementsScreenExtension.achievehunter$getWindowHeight() / 16 + 1;
        }
        return originalValue;
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V"
        )
    )
    private void cancelBackgroundRenderInSearch(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int x,
        int y,
        float u,
        float v,
        int width,
        int height,
        int textureWidth,
        int textureHeight,
        Operation<Void> original
    ) {
        if (screen instanceof AdvancementsScreenExtension advancementsScreenExtension &&
            !advancementsScreenExtension.achievehunter$isSearchActive()
        ) {
            original.call(context, renderLayers, sprite, x, y, u, v, width, height, textureWidth, textureHeight);
        }
    }
}
