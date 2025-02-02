package com.diskree.achievehunter.injection.mixin;

import com.diskree.achievehunter.AchieveHunterMod;
import com.diskree.achievehunter.Constants;
import com.diskree.achievehunter.injection.extension.AdvancementWidgetExtension;
import com.diskree.achievehunter.injection.extension.AdvancementsScreenExtension;
import com.diskree.achievehunter.util.AdvancementWidgetRenderPriority;
import com.diskree.achievehunter.util.HighlightType;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.PlacedAdvancement;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementObtainedStatus;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
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
    private Boolean forceMirrorTooltipHorizontally;

    @Unique
    private Boolean forceMirrorTooltipVertically;

    @Unique
    private boolean isTooltipMirroredHorizontally;

    @Unique
    private boolean isTooltipMirroredVertically;

    @Unique
    private int tooltipHeight = Integer.MIN_VALUE;

    @Unique
    private int tooltipDescriptionHeight = Integer.MIN_VALUE;

    @Unique
    private float horizontallySwapAnimationProgress;

    @Unique
    private float verticallySwapAnimationProgress;

    @Unique
    private boolean shouldOnlyCalculateTooltipHeightOnNextRender;

    @Unique
    private boolean isCollapsed;

    @Unique
    private void applyRenderPriority(@NotNull DrawContext context, @NotNull AdvancementWidgetRenderPriority priority) {
        context.getMatrices().translate(0, 0, (priority.ordinal() + 1) * 100);
    }

    @Override
    public boolean achievehunter$isTooltipMirroredHorizontally() {
        return isTooltipMirroredHorizontally;
    }

    @Override
    public boolean achievehunter$isTooltipMirroredVertically() {
        return isTooltipMirroredVertically;
    }

    @Override
    public void achievehunter$setForceMirrorTooltipHorizontally(Boolean forceMirrorTooltipHorizontally) {
        this.forceMirrorTooltipHorizontally = forceMirrorTooltipHorizontally;
    }

    @Override
    public void achievehunter$setForceMirrorTooltipVertically(Boolean forceMirrorTooltipVertically) {
        this.forceMirrorTooltipVertically = forceMirrorTooltipVertically;
    }

    @Override
    public int achievehunter$getTooltipWidth() {
        return width - 1;
    }

    @Override
    public int achievehunter$getTooltipHeight(boolean withDescription) {
        if (tooltipHeight == Integer.MIN_VALUE || tooltipDescriptionHeight == Integer.MIN_VALUE) {
            shouldOnlyCalculateTooltipHeightOnNextRender = true;
            drawTooltip(null, 0, 0, 1.0f, 0, 0);
            shouldOnlyCalculateTooltipHeightOnNextRender = false;
        }
        int result = tooltipHeight;
        if (!withDescription) {
            result -= tooltipDescriptionHeight;
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
    public void achievehunter$setHorizontallySwapAnimationProgress(float horizontallySwapAnimationProgress) {
        this.horizontallySwapAnimationProgress = horizontallySwapAnimationProgress;
    }

    @Override
    public void achievehunter$setVerticallySwapAnimationProgress(float verticallySwapAnimationProgress) {
        this.verticallySwapAnimationProgress = verticallySwapAnimationProgress;
    }

    @Override
    public void achievehunter$setCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
        tooltipHeight = tooltipDescriptionHeight = Integer.MIN_VALUE;
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
            shift = At.Shift.AFTER
        )
    )
    public void saveMirroringFlags(
        DrawContext context,
        int originX,
        int originY,
        float alpha,
        int x,
        int y,
        CallbackInfo ci,
        @Local(ordinal = 0) boolean isMirroredHorizontally,
        @Local(ordinal = 1) boolean isMirroredVertically
    ) {
        isTooltipMirroredHorizontally = isMirroredHorizontally;
        isTooltipMirroredVertically = isMirroredVertically;
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
        if (shouldOnlyCalculateTooltipHeightOnNextRender) {
            titleHeight -= Constants.ADVANCEMENT_FRAME_OVERHANG * 2;
            if (isCollapsed) {
                descriptionHeight = 0;
            }
            tooltipHeight = titleHeight + descriptionHeight;
            tooltipDescriptionHeight = descriptionHeight;
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
        if (forceMirrorTooltipHorizontally != null) {
            isMirroredHorizontallyRef.set(forceMirrorTooltipHorizontally);
        }
        if (forceMirrorTooltipVertically != null) {
            isMirroredVerticallyRef.set(forceMirrorTooltipVertically);
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
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
            ordinal = 2
        )
    )
    public void wrapProgressBarRender(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int progressBarX,
        int progressBarY,
        int progressBarWidth,
        int progressBarHeight,
        @NotNull Operation<Void> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        boolean shouldApplyRenderPriority = false;
        if (verticallySwapAnimationProgress != 0.0f) {
            progressBarY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + achievehunter$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3,
                progressBarY
            );
            shouldApplyRenderPriority = true;
        }
        if (shouldApplyRenderPriority) {
            context.getMatrices().push();
            applyRenderPriority(context, AdvancementWidgetRenderPriority.PROGRESS_BAR);
        }
        original.call(context, renderLayers, sprite, progressBarX, progressBarY, progressBarWidth, progressBarHeight);
        if (shouldApplyRenderPriority) {
            context.getMatrices().pop();
        }
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIIIIIII)V"
        )
    )
    public void wrapProgressBarRender(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int textureWidth,
        int textureHeight,
        int textureU,
        int textureV,
        int partX,
        int partY,
        int partWidth,
        int partHeight,
        @NotNull Operation<Void> original,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        boolean shouldApplyRenderPriority = false;
        if (verticallySwapAnimationProgress != 0.0f) {
            partY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + achievehunter$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3,
                partY
            );
            shouldApplyRenderPriority = true;
        }
        if (shouldApplyRenderPriority) {
            context.getMatrices().push();
            applyRenderPriority(context, AdvancementWidgetRenderPriority.PROGRESS_BAR);
        }
        original.call(
            context,
            renderLayers,
            sprite,
            textureWidth,
            textureHeight,
            textureU,
            textureV,
            partX,
            partY,
            partWidth,
            partHeight
        );
        if (shouldApplyRenderPriority) {
            context.getMatrices().pop();
        }
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
            ordinal = 3
        )
    )
    public void wrapFrameRender(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int frameX,
        int frameY,
        int frameWidth,
        int frameHeight,
        @NotNull Operation<Void> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        boolean shouldApplyRenderPriority = false;
        if (horizontallySwapAnimationProgress != 0.0f) {
            frameX = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                originX + x + achievehunter$getTooltipWidth() - Constants.ADVANCEMENT_FRAME_SIZE - 3,
                frameX
            );
            shouldApplyRenderPriority = true;
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            frameY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + achievehunter$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3,
                frameY
            );
            shouldApplyRenderPriority = true;
        }

        if (shouldApplyRenderPriority) {
            context.getMatrices().push();
            applyRenderPriority(context, AdvancementWidgetRenderPriority.FRAME);
        }
        original.call(context, renderLayers, sprite, frameX, frameY, frameWidth, frameHeight);
        if (shouldApplyRenderPriority) {
            context.getMatrices().pop();
        }
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementWidget;drawText(Lnet/minecraft/client/gui/DrawContext;Ljava/util/List;III)V",
            ordinal = 1
        )
    )
    public void wrapTitleRender(
        AdvancementWidget widget,
        DrawContext context,
        List<OrderedText> text,
        int titleX,
        int titleY,
        int color,
        @NotNull Operation<Void> original,
        @Local(ordinal = 13) int s,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        boolean shouldApplyRenderPriority = false;
        if (horizontallySwapAnimationProgress != 0.0f) {
            titleX = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                s,
                titleX
            );
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            titleY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + achievehunter$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3 + 8,
                titleY
            );
            shouldApplyRenderPriority = true;
        }
        if (shouldApplyRenderPriority) {
            context.getMatrices().push();
            applyRenderPriority(context, AdvancementWidgetRenderPriority.TITLE);
        }
        original.call(widget, context, text, titleX, titleY, color);
        if (shouldApplyRenderPriority) {
            context.getMatrices().pop();
        }
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I",
            ordinal = 1
        )
    )
    public int wrapProgressTextRender(
        DrawContext context,
        TextRenderer textRenderer,
        Text text,
        int progressTextX,
        int progressTextY,
        int color,
        Operation<Integer> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY,
        @Local(ordinal = 9) int progressTextWidth
    ) {
        boolean shouldApplyRenderPriority = false;
        if (horizontallySwapAnimationProgress != 0.0f) {
            progressTextX = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                originX + x + achievehunter$getTooltipWidth() - progressTextWidth - Constants.ADVANCEMENT_FRAME_SIZE - 6,
                progressTextX
            );
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            progressTextY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + achievehunter$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3 + 8,
                progressTextY
            );
            shouldApplyRenderPriority = true;
        }
        if (shouldApplyRenderPriority) {
            context.getMatrices().push();
            applyRenderPriority(context, AdvancementWidgetRenderPriority.PROGRESS_TEXT);
        }
        int result = original.call(context, textRenderer, text, progressTextX, progressTextY, color);
        if (shouldApplyRenderPriority) {
            context.getMatrices().pop();
        }
        return result;
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawItemWithoutEntity(Lnet/minecraft/item/ItemStack;II)V"
        )
    )
    public void wrapIconRender(
        DrawContext context,
        ItemStack stack,
        int itemX,
        int itemY,
        Operation<Void> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        boolean shouldApplyRenderPriority = false;
        if (horizontallySwapAnimationProgress != 0.0f) {
            itemX = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                originX + x + achievehunter$getTooltipWidth() - Constants.ADVANCEMENT_FRAME_SIZE - 3 + 5,
                itemX
            );
            shouldApplyRenderPriority = true;
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            itemY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + achievehunter$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 5 + 3,
                itemY
            );
            shouldApplyRenderPriority = true;
        }
        if (shouldApplyRenderPriority) {
            context.getMatrices().push();
            applyRenderPriority(context, AdvancementWidgetRenderPriority.ICON);
        }
        original.call(context, stack, itemX, itemY);
        if (shouldApplyRenderPriority) {
            context.getMatrices().pop();
        }
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
        boolean shouldApplyRenderPriority = false;
        if (verticallySwapAnimationProgress != 0.0f) {
            descriptionY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + 7,
                descriptionY
            );
            shouldApplyRenderPriority = true;
        }
        if (shouldApplyRenderPriority) {
            context.getMatrices().push();
            applyRenderPriority(context, AdvancementWidgetRenderPriority.DESCRIPTION);
        }
        original.call(widget, context, text, descriptionX, descriptionY, color);
        if (shouldApplyRenderPriority) {
            context.getMatrices().pop();
        }
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
