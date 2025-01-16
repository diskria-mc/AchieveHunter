package com.diskree.advancementsexplorer.injection.mixin;

import com.diskree.advancementsexplorer.Constants;
import com.diskree.advancementsexplorer.injection.extension.AdvancementWidgetExtension;
import com.diskree.advancementsexplorer.injection.extension.AdvancementsScreenExtension;
import com.diskree.advancementsexplorer.util.AdvancementsScreenState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancement.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin extends Screen implements AdvancementsScreenExtension {

    @Unique
    private static final int FOCUSED_ADVANCEMENT_CLICK_TIMEOUT_TICKS = 10;

    @Unique
    private static final double DRAG_THRESHOLD = 5.0d;

    @Unique
    private static final float ANIMATION_DURATION_SECONDS = 0.8f;

    @Unique
    private static final int WINDOW_BORDER_SIZE = 9;

    @Unique
    private static final int WINDOW_HEADER_HEIGHT = 18;

    @Unique
    private AdvancementsScreenState viewState = AdvancementsScreenState.WINDOW_VISIBLE;

    @Unique
    private int windowTreeX;

    @Unique
    private int windowTreeY;

    @Unique
    @Nullable
    private PlacedAdvancement focusedPlacedAdvancement;

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
    @Nullable
    private AdvancementWidget transitionAdvancementWidget;

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

    @Unique
    private List<PlacedAdvancement> carouselAdvancements;

    @Unique
    private final Map<PlacedAdvancement, AdvancementWidget> carouselWidgets = new HashMap<>();

    @Unique
    private int carouselCenterIndex = -1;

    protected AdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void startTransitionToCarousel() {
        if (focusedAdvancementWidget == null) {
            return;
        }
        carouselCenterIndex = getCarouselAdvancements().indexOf(focusedPlacedAdvancement);
        if (carouselCenterIndex == -1) {
            return;
        }
        viewState = AdvancementsScreenState.TRANSITION_TO_CAROUSEL;
        tooltipTransitionProgress = 0.0f;
    }

    @Unique
    private void startTransitionToWindow() {
        viewState = AdvancementsScreenState.TRANSITION_TO_WINDOW;
        tooltipTransitionProgress = 0.0f;
    }

    @Unique
    private @NotNull List<PlacedAdvancement> getCarouselAdvancements() {
        if (carouselAdvancements == null) {
            carouselAdvancements = new ArrayList<>();
            for (PlacedAdvancement advancement : new ArrayList<>(advancementHandler.getManager().getAdvancements())) {
                if (isCarouselAdvancement(advancement)) {
                    carouselAdvancements.add(advancement);
                }
            }
            carouselAdvancements.sort(
                Comparator.comparing((PlacedAdvancement advancement) -> {
                        AdvancementProgress progress = advancementHandler.advancementProgresses.get(advancement.getAdvancementEntry());
                        return progress.isDone() ? Float.MAX_VALUE : 1.0f - progress.getProgressBarPercentage();
                    })
                    .thenComparing(advancement -> advancement.getAdvancementEntry().id().toString())
            );
        }
        return carouselAdvancements;
    }

    @Unique
    private boolean isCarouselAdvancement(@Nullable PlacedAdvancement placedAdvancement) {
        if (placedAdvancement == null) {
            return false;
        }
        AdvancementEntry advancementEntry = placedAdvancement.getAdvancementEntry();
        Advancement advancement = advancementEntry.value();
        if (advancement.isRoot()) {
            return false;
        }
        AdvancementDisplay display = advancementEntry.value().display().orElse(null);
        if (display == null) {
            return false;
        }
        if (advancement.requirements().requirements().size() <= 1) {
            return false;
        }
        if (display.isHidden()) {
            AdvancementProgress progress = advancementHandler.advancementProgresses.get(advancementEntry);
            return progress != null && progress.isDone();
        }
        return true;
    }

    @Override
    public void advancementsexplorer$tick() {
        if (focusedAdvancementReleaseClickTimer > 0) {
            focusedAdvancementReleaseClickTimer--;
            if (focusedAdvancementReleaseClickTimer == 0) {
                isFocusedAdvancementClicked = false;
                focusedPlacedAdvancement = null;
                focusedAdvancementWidget = null;
            }
        }
    }

    @Override
    public boolean advancementsexplorer$charTyped(char chr, int modifiers) {
        return viewState.isTransitionState();
    }

    @Override
    public void advancementsexplorer$setFocusedAdvancement(@Nullable AdvancementWidget advancementWidget) {
        PlacedAdvancement placedAdvancement = advancementWidget != null ? advancementWidget.advancement : null;
        if (viewState != AdvancementsScreenState.WINDOW_VISIBLE &&
            Objects.equals(focusedPlacedAdvancement, placedAdvancement)
        ) {
            return;
        }
        if (advancementWidget != null && !isCarouselAdvancement(advancementWidget.advancement)) {
            return;
        }
        focusedPlacedAdvancement = placedAdvancement;
        focusedAdvancementWidget = advancementWidget;
    }

    @Override
    public void advancementsexplorer$onMouseReleased(double mouseX, double mouseY, int button) {
        if (focusedPlacedAdvancement != null &&
            isFocusedAdvancementClicked &&
            button == GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) {
            double deltaX = mouseX - focusedAdvancementClickX;
            double deltaY = mouseY - focusedAdvancementClickY;
            if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) < DRAG_THRESHOLD) {
                startTransitionToCarousel();
            }
            isFocusedAdvancementClicked = false;
        }
    }

    @Shadow
    @Final
    private ClientAdvancementManager advancementHandler;

    @Shadow
    private @Nullable AdvancementTab selectedTab;

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
        if (viewState != AdvancementsScreenState.WINDOW_VISIBLE) {
            cir.setReturnValue(false);
        }
        if (focusedPlacedAdvancement != null && !isFocusedAdvancementClicked && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
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
        if (viewState != AdvancementsScreenState.WINDOW_VISIBLE) {
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
        if (viewState != AdvancementsScreenState.WINDOW_VISIBLE) {
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
        if (viewState.isTransitionState()) {
            tooltipTransitionProgress += (delta / 20.0f) / ANIMATION_DURATION_SECONDS;
            if (tooltipTransitionProgress >= 1.0f) {
                tooltipTransitionProgress = 1.0f;
                viewState = viewState == AdvancementsScreenState.TRANSITION_TO_CAROUSEL
                    ? AdvancementsScreenState.CAROUSEL_VISIBLE
                    : AdvancementsScreenState.WINDOW_VISIBLE;
                transitionAdvancementWidget = null;
            }
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
                    tooltipTransitionEndX = width / 4 - tooltipWidth / 2;
                    tooltipTransitionEndY = height / 2 - tooltipHeight / 2;
                    shouldHorizontallySwapTransitionTooltip =
                        advancementWidgetExtension.advancementsexplorer$isTooltipMirroredHorizontally();
                    shouldVerticallySwapTransitionTooltip =
                        advancementWidgetExtension.advancementsexplorer$isTooltipMirroredVertically();
                    widgetTransitionX =
                        windowTreeX + MathHelper.floor(focusedAdvancementTab.originX) + focusedAdvancementWidget.x;
                    widgetTransitionY =
                        windowTreeY + MathHelper.floor(focusedAdvancementTab.originY) + focusedAdvancementWidget.y;
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
                float middleX = width / 2f;
                float middleY = height / 2f;
                float progress = tooltipTransitionProgress;
                float inverse = 1.0F - progress;
                float startX = widgetTransitionX;
                float endX = tooltipTransitionEndX;

                float newX = inverse * inverse * startX
                    + 2.0F * inverse * progress * middleX
                    + progress * progress * endX;

                float startY = widgetTransitionY;
                float endY = tooltipTransitionEndY;

                float newY = inverse * inverse * startY
                    + 2.0F * inverse * progress * middleY
                    + progress * progress * endY;

                advancementWidgetExtension.advancementsexplorer$setX((int) newX);
                advancementWidgetExtension.advancementsexplorer$setY((int) newY);

                if (shouldHorizontallySwapTransitionTooltip) {
                    advancementWidgetExtension
                        .advancementsexplorer$setHorizontallySwapAnimationProgress(tooltipTransitionProgress);
                }
                if (shouldVerticallySwapTransitionTooltip) {
                    advancementWidgetExtension
                        .advancementsexplorer$setVerticallySwapAnimationProgress(tooltipTransitionProgress);
                }
                transitionAdvancementWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
            }
        }
        if (viewState != AdvancementsScreenState.WINDOW_VISIBLE) {
            if (viewState == AdvancementsScreenState.CAROUSEL_VISIBLE) {
                PlacedAdvancement centerAdvancement = carouselAdvancements.get(carouselCenterIndex);
                if (centerAdvancement != null && selectedTab != null && client != null) {
                    AdvancementDisplay display = centerAdvancement.getAdvancement().display().orElse(null);
                    if (display != null) {
                        AdvancementWidget centerWidget = carouselWidgets.computeIfAbsent(centerAdvancement, k ->
                            new AdvancementWidget(selectedTab, client, centerAdvancement, display)
                        );
                        centerWidget.setProgress(advancementHandler.advancementProgresses.get(centerAdvancement.getAdvancementEntry()));
                        if (centerWidget instanceof AdvancementWidgetExtension advancementWidgetExtension) {
                            advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipHorizontally(false);
                            advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipVertically(false);
                            advancementWidgetExtension.advancementsexplorer$setX(tooltipTransitionEndX);
                            advancementWidgetExtension.advancementsexplorer$setY(tooltipTransitionEndY);
                        }
                        centerWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
                    }
                }
            }
            ci.cancel();
        }
    }

    @Inject(
        method = "keyPressed",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void ignoreKeyPressed(
        int keyCode,
        int scanCode,
        int modifiers,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (viewState == AdvancementsScreenState.CAROUSEL_VISIBLE) {
                startTransitionToWindow();
                cir.setReturnValue(true);
            } else if (viewState == AdvancementsScreenState.TRANSITION_TO_CAROUSEL) {
                cir.setReturnValue(true);
            }
        } else {
            if (viewState.isTransitionState()) {
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
