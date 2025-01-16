package com.diskree.advancementsexplorer.injection.mixin;

import com.diskree.advancementsexplorer.Constants;
import com.diskree.advancementsexplorer.injection.extension.AdvancementWidgetExtension;
import com.diskree.advancementsexplorer.injection.extension.AdvancementsScreenExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin extends Screen implements AdvancementsScreenExtension {

    @Unique
    private static final int FOCUSED_ADVANCEMENT_CLICK_TIMEOUT_TICKS = 10;

    @Unique
    private static final double DRAG_THRESHOLD = 5.0d;

    @Unique
    private static final long ANIMATION_DURATION_MS = 500;

    @Unique
    private static final int WINDOW_BORDER_SIZE = 9;

    @Unique
    private static final int WINDOW_HEADER_HEIGHT = 18;

    @Unique
    private int windowTreeX;

    @Unique
    private int windowTreeY;

    @Unique
    @Nullable
    private Identifier focusedAdvancementId;

    @Unique
    @Nullable
    private AdvancementWidget focusedAdvancementWidget;

    @Unique
    private boolean isFocusedAdvancementClicked;

    @Unique
    private double focusedAdvancementClickX;

    @Unique
    private double focusedAdvancementClickY;

    @Unique
    private int focusedAdvancementReleaseClickTimer;

    @Unique
    private float windowTranslationY;

    @Unique
    private float windowAnimationFromY;

    @Unique
    private float windowAnimationToY;

    @Unique
    private long windowAnimationStartTime;

    @Unique
    private long windowAnimationEndTime;

    @Unique
    private boolean isWindowAnimationInProgress;

    @Unique
    private boolean isWindowInHideAnimation;

    @Unique
    private boolean isWindowFullyHidden;

    @Unique
    @Nullable
    private AdvancementWidget transitionAdvancementWidget;

    @Unique
    private int tooltipTransitionX;

    @Unique
    private int tooltipTransitionY;

    @Unique
    private int widgetTransitionX;

    @Unique
    private int widgetTransitionY;

    @Unique
    private boolean shouldHorizontallySwapTransitionTooltip;

    @Unique
    private boolean shouldVerticallySwapTransitionTooltip;

    @Unique
    private int tooltipTransitionEndX;

    @Unique
    private int tooltipTransitionEndY;

    @Unique
    private float tooltipTransitionProgress;

    protected AdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Unique
    public void hideWindow() {
        isWindowAnimationInProgress = true;
        isWindowInHideAnimation = true;
        windowAnimationFromY = windowTranslationY;
        windowAnimationToY = -height;
        windowAnimationStartTime = System.currentTimeMillis();
        windowAnimationEndTime = windowAnimationStartTime + ANIMATION_DURATION_MS;
    }

    @Unique
    public void showWindow() {
        isWindowAnimationInProgress = true;
        isWindowInHideAnimation = false;
        windowAnimationFromY = windowTranslationY;
        windowAnimationToY = 0.0f;
        windowAnimationStartTime = System.currentTimeMillis();
        windowAnimationEndTime = windowAnimationStartTime + ANIMATION_DURATION_MS;
    }

    @Override
    public void advancementsexplorer$tick() {
        if (focusedAdvancementReleaseClickTimer > 0) {
            focusedAdvancementReleaseClickTimer--;
            if (focusedAdvancementReleaseClickTimer == 0) {
                isFocusedAdvancementClicked = false;
                focusedAdvancementId = null;
                focusedAdvancementWidget = null;
            }
        }
    }

    @Override
    public boolean advancementsexplorer$charTyped(char chr, int modifiers) {
        if (windowTranslationY != 0.0f) {
            return true;
        }
        return false;
    }

    @Override
    public void advancementsexplorer$setFocusedAdvancement(
        @Nullable Identifier advancementId,
        @Nullable AdvancementWidget advancementWidget
    ) {
        if (windowTranslationY != 0.0f || Objects.equals(focusedAdvancementId, advancementId)) {
            return;
        }
        focusedAdvancementId = advancementId;
        focusedAdvancementWidget = advancementWidget;
    }

    @Override
    public void advancementsexplorer$onMouseReleased(double mouseX, double mouseY, int button) {
        if (focusedAdvancementId != null && isFocusedAdvancementClicked && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            double deltaX = mouseX - focusedAdvancementClickX;
            double deltaY = mouseY - focusedAdvancementClickY;
            if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) < DRAG_THRESHOLD) {
                hideWindow();
            }
            isFocusedAdvancementClicked = false;
        }
    }

    @Inject(
        method = "mouseClicked",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void onMouseClicked(
        double mouseX,
        double mouseY,
        int button,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (isWindowAnimationInProgress) {
            cir.setReturnValue(false);
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && focusedAdvancementId != null && !isFocusedAdvancementClicked) {
            focusedAdvancementClickX = mouseX;
            focusedAdvancementClickY = mouseY;
            focusedAdvancementReleaseClickTimer = FOCUSED_ADVANCEMENT_CLICK_TIMEOUT_TICKS;
            isFocusedAdvancementClicked = true;
        } else {
            isFocusedAdvancementClicked = false;
        }
    }

    @Inject(
        method = "mouseDragged",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void onMouseDragged(
        double mouseX,
        double mouseY,
        int button,
        double deltaX,
        double deltaY,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (isWindowAnimationInProgress) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "mouseScrolled",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void onMouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (isWindowAnimationInProgress) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    public void moveWindow(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isWindowAnimationInProgress) {
            long totalTime = windowAnimationEndTime - windowAnimationStartTime;
            long elapsedTime = System.currentTimeMillis() - windowAnimationStartTime;
            tooltipTransitionProgress = Math.min(1.0f, (float) elapsedTime / (float) totalTime);
            if (tooltipTransitionProgress >= 1.0f) {
                tooltipTransitionProgress = 1.0f;
                isWindowAnimationInProgress = false;
                transitionAdvancementWidget = null;
            }
            windowTranslationY = MathHelper.lerp(tooltipTransitionProgress, windowAnimationFromY, windowAnimationToY);
            isWindowFullyHidden = windowTranslationY <= -height;
        }
        if (isWindowFullyHidden) {
            ci.cancel();
            return;
        }
        if (windowTranslationY != 0.0f) {
            context.getMatrices().push();
            context.getMatrices().translate(0, windowTranslationY, 0);
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;drawWidgetTooltip(Lnet/minecraft/client/gui/DrawContext;IIII)V",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    public void restoreMatricesAfterWindowTranslate(
        DrawContext context,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        if (windowTranslationY == 0.0f) {
            return;
        }
        MatrixStack matrices = context.getMatrices();
        matrices.pop();
        if (transitionAdvancementWidget == null && focusedAdvancementWidget != null && client != null) {
            AdvancementTab focusedAdvancementTab = focusedAdvancementWidget.tab;
            transitionAdvancementWidget = new AdvancementWidget(
                focusedAdvancementTab,
                client,
                focusedAdvancementWidget.advancement,
                focusedAdvancementWidget.display
            );
            transitionAdvancementWidget.setProgress(focusedAdvancementWidget.progress);
            if (focusedAdvancementWidget instanceof AdvancementWidgetExtension advancementWidgetExtension) {
                int tooltipWidth = focusedAdvancementWidget.getWidth();
                int tooltipHeight = advancementWidgetExtension.advancementsexplorer$getTooltipHeight();
                tooltipTransitionX = windowTreeX + advancementWidgetExtension.advancementsexplorer$getTooltipX();
                tooltipTransitionY = windowTreeY + advancementWidgetExtension.advancementsexplorer$getTooltipY();
                tooltipTransitionEndX = width / 4 - tooltipWidth / 2;
                tooltipTransitionEndY = height / 2 - tooltipHeight / 2;
                shouldHorizontallySwapTransitionTooltip = advancementWidgetExtension.advancementsexplorer$isTooltipMirroredHorizontally();
                shouldVerticallySwapTransitionTooltip = advancementWidgetExtension.advancementsexplorer$isTooltipMirroredVertically();
                widgetTransitionX = windowTreeX + MathHelper.floor(focusedAdvancementTab.originX) + focusedAdvancementWidget.x;
                widgetTransitionY = windowTreeY + MathHelper.floor(focusedAdvancementTab.originY) + focusedAdvancementWidget.y;
                if (shouldHorizontallySwapTransitionTooltip) {
                    widgetTransitionX -= tooltipWidth;
                    widgetTransitionX += Constants.ADVANCEMENT_FRAME_SIZE + Constants.ADVANCEMENT_FRAME_OVERHANG * 2;
                }
                if (shouldVerticallySwapTransitionTooltip) {
                    widgetTransitionY -= tooltipHeight;
                    widgetTransitionY += Constants.ADVANCEMENT_FRAME_SIZE - Constants.ADVANCEMENT_FRAME_OVERHANG * 2;
                }
            }
            if (transitionAdvancementWidget instanceof AdvancementWidgetExtension advancementWidgetExtension) {
                advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipHorizontally(false);
                advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipVertically(false);
                advancementWidgetExtension.advancementsexplorer$setX(widgetTransitionX);
                advancementWidgetExtension.advancementsexplorer$setY(widgetTransitionY);
            }
            focusedAdvancementWidget = null;
        }
        if (transitionAdvancementWidget instanceof AdvancementWidgetExtension advancementWidgetExtension) {
            matrices.push();
            matrices.translate(0, 0, 1000);
            if (shouldHorizontallySwapTransitionTooltip) {
                advancementWidgetExtension.advancementsexplorer$setHorizontallySwapAnimationProgress(tooltipTransitionProgress);
            }
            if (shouldVerticallySwapTransitionTooltip) {
                advancementWidgetExtension.advancementsexplorer$setVerticallySwapAnimationProgress(tooltipTransitionProgress);
            }
            transitionAdvancementWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
            matrices.pop();
        }
        ci.cancel();
    }

    @Inject(
        method = "keyPressed",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void showWindowOnEscape(
        int keyCode,
        int scanCode,
        int modifiers,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (isWindowFullyHidden) {
                showWindow();
                cir.setReturnValue(true);
            } else if (isWindowInHideAnimation) {
                cir.setReturnValue(true);
            }
        } else {
            if (isWindowAnimationInProgress) {
                cir.setReturnValue(true);
            }
        }
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;drawAdvancementTree(Lnet/minecraft/client/gui/DrawContext;IIII)V"
        )
    )
    public void getTreeCoordinates(
        AdvancementsScreen screen,
        DrawContext context,
        int mouseX,
        int mouseY,
        int x,
        int y,
        @NotNull Operation<Void> original
    ) {
        windowTreeX = x + WINDOW_BORDER_SIZE;
        windowTreeY = y + WINDOW_HEADER_HEIGHT;
        original.call(screen, context, mouseX, mouseY, x, y);
    }
}
