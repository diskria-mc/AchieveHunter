package com.diskree.advancementsexplorer.injection.mixin;

import com.diskree.advancementsexplorer.Constants;
import com.diskree.advancementsexplorer.injection.extension.AdvancementWidgetExtension;
import com.diskree.advancementsexplorer.util.AdvancementWidgetRenderPriority;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

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
    private void applyRenderPriority(@NotNull DrawContext context, @NotNull AdvancementWidgetRenderPriority priority) {
        context.getMatrices().translate(0, 0, (priority.ordinal() + 1) * 100);
    }

    @Override
    public boolean advancementsexplorer$isTooltipMirroredHorizontally() {
        return isTooltipMirroredHorizontally;
    }

    @Override
    public boolean advancementsexplorer$isTooltipMirroredVertically() {
        return isTooltipMirroredVertically;
    }

    @Override
    public void advancementsexplorer$setForceMirrorTooltipHorizontally(Boolean forceMirrorTooltipHorizontally) {
        this.forceMirrorTooltipHorizontally = forceMirrorTooltipHorizontally;
    }

    @Override
    public void advancementsexplorer$setForceMirrorTooltipVertically(Boolean forceMirrorTooltipVertically) {
        this.forceMirrorTooltipVertically = forceMirrorTooltipVertically;
    }

    @Override
    public int advancementsexplorer$getTooltipWidth() {
        return width - 1;
    }

    @Override
    public int advancementsexplorer$getTooltipHeight(boolean withDescription) {
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
    public void advancementsexplorer$setX(int x) {
        this.x = x;
    }

    @Override
    public void advancementsexplorer$setY(int y) {
        this.y = y;
    }

    @Override
    public void advancementsexplorer$setHorizontallySwapAnimationProgress(float horizontallySwapAnimationProgress) {
        this.horizontallySwapAnimationProgress = horizontallySwapAnimationProgress;
    }

    @Override
    public void advancementsexplorer$setVerticallySwapAnimationProgress(float verticallySwapAnimationProgress) {
        this.verticallySwapAnimationProgress = verticallySwapAnimationProgress;
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
            titleHeight -= 6;
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
                originY + y + advancementsexplorer$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3,
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
                originY + y + advancementsexplorer$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3,
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
                originX + x + advancementsexplorer$getTooltipWidth() - Constants.ADVANCEMENT_FRAME_SIZE - 3,
                frameX
            );
            shouldApplyRenderPriority = true;
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            frameY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + advancementsexplorer$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3,
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
                originY + y + advancementsexplorer$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3 + 8,
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
                originX + x + advancementsexplorer$getTooltipWidth() - progressTextWidth - Constants.ADVANCEMENT_FRAME_SIZE - 6,
                progressTextX
            );
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            progressTextY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + advancementsexplorer$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 3 + 8,
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
                originX + x + advancementsexplorer$getTooltipWidth() - Constants.ADVANCEMENT_FRAME_SIZE - 3 + 5,
                itemX
            );
            shouldApplyRenderPriority = true;
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            itemY = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + y + advancementsexplorer$getTooltipHeight(true) - Constants.ADVANCEMENT_FRAME_SIZE + 5 + 3,
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
}
