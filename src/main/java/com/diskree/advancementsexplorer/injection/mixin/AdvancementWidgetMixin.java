package com.diskree.advancementsexplorer.injection.mixin;

import com.diskree.advancementsexplorer.Constants;
import com.diskree.advancementsexplorer.injection.extension.AdvancementWidgetExtension;
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
public class AdvancementWidgetMixin implements AdvancementWidgetExtension {

    @Unique
    private Boolean forceMirrorTooltipHorizontally;

    @Unique
    private Boolean forceMirrorTooltipVertically;

    @Unique
    private boolean isTooltipMirroredHorizontally;

    @Unique
    private boolean isTooltipMirroredVertically;

    @Unique
    private int tooltipHeight;

    @Unique
    private int tooltipX = Integer.MAX_VALUE;

    @Unique
    private int tooltipY = Integer.MAX_VALUE;

    @Unique
    private float horizontallySwapAnimationProgress;

    @Unique
    private float verticallySwapAnimationProgress;

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
    public int advancementsexplorer$getTooltipHeight() {
        return tooltipHeight;
    }

    @Override
    public int advancementsexplorer$getTooltipX() {
        return tooltipX;
    }

    @Override
    public int advancementsexplorer$getTooltipY() {
        return tooltipY;
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

    @Shadow
    @Final
    private List<OrderedText> description;

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

    @Inject(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/MathHelper;floor(F)I",
            shift = At.Shift.AFTER
        )
    )
    public void saveRenderParameters(
        DrawContext context,
        int originX,
        int originY,
        float alpha,
        int x,
        int y,
        CallbackInfo ci,
        @Local(ordinal = 0) boolean isMirroredHorizontally,
        @Local(ordinal = 1) boolean isMirroredVertically,
        @Local(ordinal = 4) int titleHeight,
        @Local(ordinal = 8) int descriptionHeight
    ) {
        isTooltipMirroredHorizontally = isMirroredHorizontally;
        isTooltipMirroredVertically = isMirroredVertically;

        tooltipHeight = titleHeight - Constants.ADVANCEMENT_FRAME_OVERHANG * 2;
        if (!description.isEmpty()) {
            tooltipHeight += descriptionHeight;
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

    @Inject(
        method = "drawTooltip",
        at = @At(value = "HEAD")
    )
    public void startTooltipCoordinatesTracking(
        DrawContext context,
        int originX,
        int originY,
        float alpha,
        int x,
        int y,
        CallbackInfo ci
    ) {
        tooltipX = tooltipY = Integer.MAX_VALUE;
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
            ordinal = 0
        )
    )
    public void wrapDescriptionBackgroundTopRender(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int x,
        int y,
        int width,
        int height,
        @NotNull Operation<Void> original,
        @Local(argsOnly = true, ordinal = 0) int originX
    ) {
        tooltipX = Math.min(tooltipX, x);
        tooltipY = Math.min(tooltipY, y);
        original.call(context, renderLayers, sprite, x, y, width, height);
    }

    @WrapOperation(
        method = "drawTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIII)V",
            ordinal = 1
        )
    )
    public void wrapDescriptionBackgroundBottomRender(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int x,
        int y,
        int width,
        int height,
        @NotNull Operation<Void> original
    ) {
        tooltipX = Math.min(tooltipX, x);
        tooltipY = Math.min(tooltipY, y);
        original.call(context, renderLayers, sprite, x, y, width, height);
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
        int x,
        int y,
        int width,
        int height,
        @NotNull Operation<Void> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        tooltipX = Math.min(tooltipX, x);
        tooltipY = Math.min(tooltipY, y);
        if (verticallySwapAnimationProgress != 0.0f) {
            y = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + this.y + this.tooltipHeight - 26 + 3,
                y
            );
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 400);
        }
        original.call(context, renderLayers, sprite, x, y, width, height);
        if (verticallySwapAnimationProgress != 0.0f) {
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
        int x,
        int y,
        int width,
        int height,
        @NotNull Operation<Void> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        boolean shouldBringToFront = false;
        if (horizontallySwapAnimationProgress != 0.0f) {
            x = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                originX + this.x + this.width - 26 - 3,
                x
            );
            shouldBringToFront = true;
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            y = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + this.y + this.tooltipHeight - 26 + 3,
                y
            );
            shouldBringToFront = true;
        }

        if (shouldBringToFront) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 600);
        }
        original.call(context, renderLayers, sprite, x, y, width, height);
        if (shouldBringToFront) {
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
    public void wrapProgressPartsRender(
        DrawContext instance,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int textureWidth,
        int textureHeight,
        int u,
        int v,
        int x,
        int y,
        int width,
        int height,
        @NotNull Operation<Void> original
    ) {
        tooltipX = Math.min(tooltipX, x);
        tooltipY = Math.min(tooltipY, y);
        original.call(instance, renderLayers, sprite, textureWidth, textureHeight, u, v, x, y, width, height);
    }

    @Inject(
        method = "drawTooltip",
        at = @At(value = "TAIL")
    )
    public void stopTooltipCoordinatesTracking(
        DrawContext context,
        int originX,
        int originY,
        float alpha,
        int x,
        int y,
        CallbackInfo ci
    ) {
        if (!isTooltipMirroredVertically) {
            tooltipY += Constants.ADVANCEMENT_FRAME_OVERHANG;
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
    public void horizontallySwapTitle(
        AdvancementWidget widget,
        DrawContext context,
        List<OrderedText> text,
        int x,
        int y,
        int color,
        @NotNull Operation<Void> original,
        @Local(ordinal = 13) int s,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        if (horizontallySwapAnimationProgress != 0.0f) {
            x = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                s,
                x
            );
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            y = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + this.y + this.tooltipHeight - 26 + 3 + 8,
                y
            );
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 500);
        }
        original.call(widget, context, text, x, y, color);
        if (verticallySwapAnimationProgress != 0.0f) {
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
    public int horizontallySwapProgressText(
        DrawContext context,
        TextRenderer textRenderer,
        Text text,
        int x,
        int y,
        int color,
        Operation<Integer> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY,
        @Local(ordinal = 9) int progressTextWidth
    ) {
        if (horizontallySwapAnimationProgress != 0.0f) {
            x = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                originX + this.x + width - progressTextWidth - 26 - 6,
                x
            );
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            y = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + this.y + this.tooltipHeight - 26 + 3 + 8,
                y
            );
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 500);
        }
        int result = original.call(context, textRenderer, text, x, y, color);
        if (verticallySwapAnimationProgress != 0.0f) {
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
    public void wrapItemIconRender(
        DrawContext context,
        ItemStack stack,
        int x,
        int y,
        Operation<Void> original,
        @Local(argsOnly = true, ordinal = 0) int originX,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        if (horizontallySwapAnimationProgress != 0.0f) {
            x = MathHelper.lerp(
                horizontallySwapAnimationProgress,
                originX + this.x + this.width - 26 - 3 + 5,
                x
            );
        }
        if (verticallySwapAnimationProgress != 0.0f) {
            y = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + this.y + this.tooltipHeight - 26 + 5 + 3,
                y
            );
        }
        if (horizontallySwapAnimationProgress != 0.0f || verticallySwapAnimationProgress != 0.0f) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 700);
        }
        original.call(context, stack, x, y);
        if (horizontallySwapAnimationProgress != 0.0f || verticallySwapAnimationProgress != 0.0f) {
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
        int x,
        int y,
        int color,
        Operation<Void> original,
        @Local(argsOnly = true, ordinal = 1) int originY
    ) {
        if (verticallySwapAnimationProgress != 0.0f) {
            y = MathHelper.lerp(
                verticallySwapAnimationProgress,
                originY + this.y + 7,
                y
            );
        }
        original.call(widget, context, text, x, y, color);
    }
}
