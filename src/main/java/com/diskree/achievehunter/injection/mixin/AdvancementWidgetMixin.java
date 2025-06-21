package com.diskree.achievehunter.injection.mixin;

import com.diskree.achievehunter.AchieveHunterMod;
import com.diskree.achievehunter.Constants;
import com.diskree.achievehunter.injection.extension.AdvancementWidgetExtension;
import com.diskree.achievehunter.injection.extension.AdvancementsScreenExtension;
import com.diskree.achievehunter.util.HighlightType;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

import static net.minecraft.client.gui.screen.advancement.AdvancementsScreen.PAGE_HEIGHT;

@Mixin(AdvancementWidget.class)
public abstract class AdvancementWidgetMixin implements AdvancementWidgetExtension {

    @Unique
    private boolean isMirrorDisabled;

    @Unique
    private boolean isCollapsed;

    @Unique
    private int tooltipTitleHeight = Integer.MIN_VALUE;

    @Unique
    private int tooltipDescriptionHeight = Integer.MIN_VALUE;

    @Unique
    private boolean shouldCalculateTooltipHeightOnNextRender;

    @Override
    public void achievehunter$disableMirroring() {
        this.isMirrorDisabled = true;
    }

    @Override
    public int achievehunter$getTooltipWidth() {
        return width - 1;
    }

    @Override
    public int achievehunter$getTooltipHeight(boolean withDescription) {
        if (tooltipTitleHeight == Integer.MIN_VALUE || tooltipDescriptionHeight == Integer.MIN_VALUE) {
            shouldCalculateTooltipHeightOnNextRender = true;
            drawTooltip(null, 0, 0, 1.0f, 0, 0);
            shouldCalculateTooltipHeightOnNextRender = false;
        }
        int result = tooltipTitleHeight;
        if (withDescription) {
            result += tooltipDescriptionHeight;
        }
        return result;
    }

    @Override
    public void achievehunter$setX(int x) {
        this.x = x;
    }

    @Override
    public void achievehunter$setY(int y) {
        this.y = y;
    }

    @Override
    public void achievehunter$setCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
        tooltipTitleHeight = tooltipDescriptionHeight = Integer.MIN_VALUE;
    }

    @Mutable
    @Shadow
    @Final
    public int x;

    @Mutable
    @Shadow
    @Final
    public int y;

    @Shadow
    @Final
    private int width;

    @Shadow
    public abstract void drawTooltip(DrawContext context, int originX, int originY, float alpha, int x, int y);

    @Shadow
    @Final
    public AdvancementTab tab;

    @Shadow
    @Final
    public PlacedAdvancement advancement;

    @Shadow
    @Final
    private List<OrderedText> title;

    @Shadow
    @Final
    public AdvancementDisplay display;

    @ModifyConstant(
        method = "drawTooltip",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 0
        )
    )
    public int drawTooltipModifyHeight(int originalValue) {
        if (tab.getScreen() instanceof AdvancementsScreenExtension screenImpl) {
            return screenImpl.achievehunter$getTreeHeight();
        }
        return originalValue;
    }

    @Inject(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;floor(F)I",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    public void calculateTooltipHeight(
        DrawContext context,
        int originX,
        int originY,
        float alpha,
        int x,
        int y,
        CallbackInfo ci,
        @Local(ordinal = 4) int titleHeight,
        @Local(ordinal = 8) int descriptionHeight
    ) {
        if (shouldCalculateTooltipHeightOnNextRender) {
            titleHeight -= Constants.ADVANCEMENT_FRAME_OVERHANG * 2;
            if (titleHeight == 20) {
                titleHeight += Constants.ADVANCEMENT_FRAME_OVERHANG;
                if (isCollapsed) {
                    titleHeight += Constants.ADVANCEMENT_FRAME_OVERHANG;
                }
            }
            tooltipTitleHeight = titleHeight;
            tooltipDescriptionHeight = isCollapsed ? 0 : descriptionHeight;

            ci.cancel();
        }
    }

    @Inject(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancement/AdvancementProgress;getProgressBarPercentage()F",
            shift = At.Shift.AFTER
        )
    )
    public void applyForceMirroringFlags(
        DrawContext context,
        int originX,
        int originY,
        float alpha,
        int x,
        int y,
        CallbackInfo ci,
        @Local(ordinal = 0) LocalBooleanRef isMirroredHorizontallyRef,
        @Local(ordinal = 1) LocalBooleanRef isMirroredVerticallyRef
    ) {
        if (isMirrorDisabled) {
            isMirroredHorizontallyRef.set(false);
            isMirroredVerticallyRef.set(false);
        }
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;isEmpty()Z"
        )
    )
    public boolean hideDescriptionWhenCollapsed(List<OrderedText> description, @NotNull Operation<Boolean> original) {
        return original.call(description) || isCollapsed;
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;drawText(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;III)V",
            ordinal = 3
        )
    )
    public void wrapDescriptionRender(
        AdvancementWidget widget,
        DrawContext context,
        List<OrderedText> text,
        int descriptionX,
        int descriptionY,
        int color,
        Operation<Void> original,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        if (isCollapsed) {
            return;
        }
        original.call(widget, context, text, descriptionX, descriptionY, color);
    }

    @Inject(
        method = "renderLines",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void cancelLinesRenderInSearch(
        DrawContext context,
        int x,
        int y,
        boolean border,
        CallbackInfo ci
    ) {
        if (AchieveHunterMod.isSearch(tab.getRoot())) {
            ci.cancel();
        }
    }

    @WrapOperation(
        method = "renderWidgets",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V"
        )
    )
    private void highlightWidget(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int x,
        int y,
        int width,
        int height,
        Operation<Void> original
    ) {
        if (tab.getScreen() instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            Identifier advancementId = advancementsScreenExtension.achievehunter$getHighlightedAdvancementId();
            if (!AchieveHunterMod.isSearch(tab.getRoot()) &&
                advancementId != null &&
                advancementId == advancement.getAdvancementEntry().id() &&
                advancementsScreenExtension.achievehunter$getHighlightType() == HighlightType.WIDGET &&
                advancementsScreenExtension.achievehunter$isHighlightAtInvisibleState()
            ) {
                return;
            }
            original.call(context, renderLayers, sprite, x, y, width, height);
        }
    }

    @Redirect(
        method = "renderWidgets",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementObtainedStatus;getFrameTexture(Lnet/minecraft/advancement/AdvancementFrame;)Lnet/minecraft/util/Identifier;"
        )
    )
    private @Nullable Identifier highlightObtainedStatus(AdvancementObtainedStatus status, AdvancementFrame frame) {
        if (tab.getScreen() instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            Identifier advancementId = advancementsScreenExtension.achievehunter$getHighlightedAdvancementId();
            if (!AchieveHunterMod.isSearch(tab.getRoot()) &&
                advancementId != null &&
                advancementId == advancement.getAdvancementEntry().id() &&
                advancementsScreenExtension.achievehunter$getHighlightType() == HighlightType.OBTAINED_STATUS &&
                advancementsScreenExtension.achievehunter$isHighlightAtInvisibleState()
            ) {
                status = status == AdvancementObtainedStatus.OBTAINED ?
                    AdvancementObtainedStatus.UNOBTAINED : AdvancementObtainedStatus.OBTAINED;
            }
        }
        return status.getFrameTexture(frame);
    }

    @Inject(
        method = "drawTooltip",
        at = @At(value = "HEAD")
    )
    public void checkHighlight(
        DrawContext context,
        int originX,
        int originY,
        float alpha,
        int x,
        int y,
        CallbackInfo ci
    ) {
        if (tab.getScreen() instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            Identifier advancementId = advancementsScreenExtension.achievehunter$getHighlightedAdvancementId();
            if (!AchieveHunterMod.isSearch(tab.getRoot()) &&
                advancementId != null &&
                advancementId == advancement.getAdvancementEntry().id()
            ) {
                advancementsScreenExtension.achievehunter$stopHighlight();
            }
        }
    }

    @ModifyReturnValue(
        method = "shouldRender",
        at = @At(value = "TAIL")
    )
    public boolean cancelTooltipRender(boolean original) {
        if (original && tab.getScreen() instanceof AdvancementsScreenExtension advancementsScreenExtension) {
            Identifier advancementId = advancementsScreenExtension.achievehunter$getHighlightedAdvancementId();
            if (!AchieveHunterMod.isSearch(tab.getRoot()) && advancementId != null) {
                return advancementId == advancement.getAdvancementEntry().id();
            }
        }
        return original;
    }
}
