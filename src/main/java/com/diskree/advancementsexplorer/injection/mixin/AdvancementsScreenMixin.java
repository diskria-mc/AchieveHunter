package com.diskree.advancementsexplorer.injection.mixin;

import com.diskree.advancementsexplorer.Constants;
import com.diskree.advancementsexplorer.injection.extension.AdvancementWidgetExtension;
import com.diskree.advancementsexplorer.injection.extension.AdvancementsScreenExtension;
import com.diskree.advancementsexplorer.util.AdvancementsScreenState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancement.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroups;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
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

import static net.minecraft.client.gui.widget.EntryListWidget.INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
import static net.minecraft.client.gui.widget.EntryListWidget.MENU_LIST_BACKGROUND_TEXTURE;

@Mixin(AdvancementsScreen.class)
public class AdvancementsScreenMixin extends Screen implements AdvancementsScreenExtension {

    @Unique
    private static final Text SEARCH_TITLE = Text.translatable("gui.recipebook.search_hint");

    @Unique
    private static final int SEARCH_FIELD_WIDTH = 90;

    @Unique
    private static final int SEARCH_FIELD_HEIGHT = 12;

    @Unique
    private static final int HEADER_MARGIN = 5;

    @Unique
    private static final int FOCUSED_ADVANCEMENT_CLICK_TIMEOUT_TICKS = 10;

    @Unique
    private static final double DRAG_THRESHOLD = 5.0d;

    @Unique
    private static final float ANIMATION_DURATION_SECONDS = 0.4f;

    @Unique
    private static final int WINDOW_BORDER_SIZE = 9;

    @Unique
    private static final int WINDOW_HEADER_HEIGHT = 18;

    @Unique
    private static final int SEARCH_FIELD_TEXT_LEFT_OFFSET = 2;

    @Unique
    private static final List<AdvancementFrame> FRAME_TYPE_PRIORITY = Arrays.asList(
        AdvancementFrame.TASK,
        AdvancementFrame.GOAL,
        AdvancementFrame.CHALLENGE
    );

    @Unique
    private AdvancementsScreenState screenState = AdvancementsScreenState.WINDOW_VISIBLE;

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
    private int tooltipTransitionStartX;

    @Unique
    private int tooltipTransitionStartY;

    @Unique
    private int tooltipTransitionEndX;

    @Unique
    private int tooltipTransitionEndY;

    @Unique
    private boolean shouldHorizontallySwapTransitionTooltip;

    @Unique
    private boolean shouldVerticallySwapTransitionTooltip;

    @Unique
    private float tooltipTransitionProgress;

    @Unique
    private List<PlacedAdvancement> advancementsList;

    @Unique
    private final Map<PlacedAdvancement, AdvancementWidget> advancementWidgetsCache = new HashMap<>();

    @Unique
    private int currentAdvancementIndex = -1;

    @Unique
    private TextFieldWidget advancementsSearchField;

    protected AdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void openInfo() {
        if (focusedAdvancementWidget == null) {
            return;
        }
        currentAdvancementIndex = getAdvancementsList().indexOf(focusedPlacedAdvancement);
        if (currentAdvancementIndex == -1) {
            return;
        }
        screenState = AdvancementsScreenState.OPENING_INFO;
        tooltipTransitionProgress = 0.0f;
    }

    @Unique
    private @NotNull List<PlacedAdvancement> getAdvancementsList() {
        if (advancementsList == null) {
            advancementsList = new ArrayList<>();
            for (PlacedAdvancement advancement : new ArrayList<>(advancementHandler.getManager().getAdvancements())) {
                if (shouldIncludeAdvancement(advancement)) {
                    advancementsList.add(advancement);
                }
            }
            Map<AdvancementEntry, AdvancementProgress> progresses = advancementHandler.advancementProgresses;
            advancementsList.sort((advancement, otherAdvancement) -> {
                AdvancementProgress progress = progresses.get(advancement.getAdvancementEntry());
                AdvancementProgress otherProgress = progresses.get(otherAdvancement.getAdvancementEntry());
                int totalRequirementsCount = progress.requirements.getLength();
                int otherTotalRequirementsCount = otherProgress.requirements.getLength();
                int obtainedAdvancementsCount = progress.countObtainedRequirements();
                int otherObtainedAdvancementsCount = otherProgress.countObtainedRequirements();
                boolean isDone = progress.isDone();
                boolean otherIsDone = otherProgress.isDone();
                boolean isAnyObtained = progress.isAnyObtained();
                boolean otherIsAnyObtained = otherProgress.isAnyObtained();
                boolean isStarted = isAnyObtained && !isDone;
                boolean otherIsStarted = otherIsAnyObtained && !otherIsDone;

                if (isStarted && otherIsStarted) {
                    int remainingRequirementsCountComparison = Integer.compare(
                        totalRequirementsCount - obtainedAdvancementsCount,
                        otherTotalRequirementsCount - otherObtainedAdvancementsCount
                    );
                    if (remainingRequirementsCountComparison != 0) {
                        return remainingRequirementsCountComparison;
                    }
                }
                if (isStarted) {
                    return -1;
                }
                if (otherIsStarted) {
                    return 1;
                }

                boolean isNotStarted = !isAnyObtained && !isDone;
                boolean otherIsNotStarted = !otherIsAnyObtained && !otherIsDone;
                if (!isNotStarted) {
                    if (otherIsNotStarted) {
                        return 1;
                    }
                } else if (!otherIsNotStarted) {
                    return -1;
                }

                AdvancementDisplay display = advancement.getAdvancement().display().orElse(null);
                AdvancementDisplay nextDisplay = otherAdvancement.getAdvancement().display().orElse(null);
                if (display == null || nextDisplay == null) {
                    return 0;
                }
                int frameIndex = FRAME_TYPE_PRIORITY.indexOf(display.getFrame());
                int nextFrameIndex = FRAME_TYPE_PRIORITY.indexOf(nextDisplay.getFrame());
                int frameIndexComparison = Integer.compare(frameIndex, nextFrameIndex);
                if (frameIndexComparison != 0) {
                    return frameIndexComparison;
                }
                int totalRequirementsCountComparison = Integer.compare(
                    totalRequirementsCount,
                    otherTotalRequirementsCount
                );
                if (totalRequirementsCountComparison != 0) {
                    return totalRequirementsCountComparison;
                }
                return advancement.getAdvancementEntry().id().compareTo(otherAdvancement.getAdvancementEntry().id());
            });
        }
        return advancementsList;
    }

    @Unique
    private boolean shouldIncludeAdvancement(@Nullable PlacedAdvancement placedAdvancement) {
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

    @Unique
    private @Nullable AdvancementWidget getListAdvancementWidget(int index) {
        if (advancementsList == null || index < 0 || index >= advancementsList.size()) {
            return null;
        }
        PlacedAdvancement placedAdvancement = advancementsList.get(index);
        if (placedAdvancement == null || selectedTab == null || client == null) {
            return null;
        }
        AdvancementDisplay display = placedAdvancement.getAdvancement().display().orElse(null);
        if (display == null) {
            return null;
        }
        AdvancementWidget widget = advancementWidgetsCache.computeIfAbsent(placedAdvancement, k ->
            new AdvancementWidget(selectedTab, client, placedAdvancement, display)
        );
        if (!(widget instanceof AdvancementWidgetExtension advancementWidgetExtension)) {
            return null;
        }
        advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipHorizontally(false);
        advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipVertically(false);
        widget.setProgress(advancementHandler.advancementProgresses.get(placedAdvancement.getAdvancementEntry()));
        return widget;
    }

    @Unique
    private void renderDarkeningSection(DrawContext context, int x, int y, int width, int height) {
        if (client == null) {
            return;
        }
        int lineWidth = 32;
        int lineHeight = 2;

        Identifier headerTexture;
        Identifier backgroundTexture;
        Identifier footerTexture;
        if (client.world == null) {
            headerTexture = HEADER_SEPARATOR_TEXTURE;
            backgroundTexture = MENU_LIST_BACKGROUND_TEXTURE;
            footerTexture = FOOTER_SEPARATOR_TEXTURE;
        } else {
            headerTexture = INWORLD_HEADER_SEPARATOR_TEXTURE;
            backgroundTexture = INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
            footerTexture = INWORLD_FOOTER_SEPARATOR_TEXTURE;
        }
        context.drawTexture(
            RenderLayer::getGuiTextured,
            headerTexture,
            x,
            y,
            0,
            0,
            width,
            lineHeight,
            lineWidth,
            lineHeight
        );
        context.drawTexture(
            RenderLayer::getGuiTextured,
            backgroundTexture,
            x,
            y + lineHeight,
            0,
            0,
            width,
            height - lineHeight * 2,
            lineWidth,
            lineWidth
        );
        context.drawTexture(
            RenderLayer::getGuiTextured,
            footerTexture,
            x,
            y + height - lineHeight,
            0,
            0,
            width,
            lineHeight,
            lineWidth,
            lineHeight
        );
    }

    @Unique
    private void searchAdvancements() {
        if (advancementsList == null || advancementsSearchField == null) {
            return;
        }
        String query = advancementsSearchField.getText();

    }

    @Unique
    private int getCenterTooltipY(@NotNull AdvancementWidgetExtension advancementWidgetExtension) {
        int listContainerHeight = height;
        int headerHeight = PAGE_OFFSET_Y;
        int footerHeight = PAGE_OFFSET_X;

        int listHeight = listContainerHeight - headerHeight - footerHeight;
        int listCenterY = headerHeight + listHeight / 2;

        int centerTooltipWidth = advancementWidgetExtension.advancementsexplorer$getTooltipWidth();
        int centerTooltipHeight = advancementWidgetExtension.advancementsexplorer$getTooltipHeight(true);
        return listCenterY - centerTooltipHeight / 2 - Constants.ADVANCEMENT_FRAME_OVERHANG;
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
        if (screenState == AdvancementsScreenState.OPENING_INFO) {
            return true;
        }
        if (screenState == AdvancementsScreenState.INFO_VISIBLE && advancementsSearchField != null) {
            String oldText = advancementsSearchField.getText();
            if (advancementsSearchField.charTyped(chr, modifiers)) {
                if (!Objects.equals(oldText, advancementsSearchField.getText())) {
                    searchAdvancements();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void advancementssearch$resize(MinecraftClient client, int width, int height) {
        if (advancementsSearchField != null) {
            String oldText = advancementsSearchField.getText();
            init(client, width, height);
            advancementsSearchField.setText(oldText);
        }
    }

    @Override
    public void advancementsexplorer$setFocusedAdvancement(@Nullable AdvancementWidget advancementWidget) {
        PlacedAdvancement placedAdvancement = advancementWidget != null ? advancementWidget.advancement : null;
        if (screenState != AdvancementsScreenState.WINDOW_VISIBLE &&
            Objects.equals(focusedPlacedAdvancement, placedAdvancement)
        ) {
            return;
        }
        if (advancementWidget != null && !shouldIncludeAdvancement(placedAdvancement)) {
            focusedPlacedAdvancement = null;
            focusedAdvancementWidget = null;
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
                openInfo();
            }
            isFocusedAdvancementClicked = false;
        }
    }

    @Shadow
    @Final
    private ClientAdvancementManager advancementHandler;

    @Shadow
    private @Nullable AdvancementTab selectedTab;

    @Shadow
    @Final
    public static Identifier WINDOW_TEXTURE;

    @Shadow
    @Final
    public static int PAGE_OFFSET_X;

    @Shadow
    @Final
    public static int PAGE_WIDTH;

    @Shadow
    @Final
    public static int PAGE_OFFSET_Y;

    @Shadow
    @Final
    public static int PAGE_HEIGHT;

    @Shadow
    @Final
    public static int WINDOW_WIDTH;

    @Shadow
    @Final
    public static int WINDOW_HEIGHT;

    @Shadow
    @Final
    private static Text ADVANCEMENTS_TEXT;

    @Inject(
        method = "init",
        at = @At(value = "TAIL")
    )
    public void initInject(CallbackInfo ci) {
        advancementsSearchField = new TextFieldWidget(
            textRenderer,
            0,
            0,
            SEARCH_FIELD_WIDTH - SEARCH_FIELD_TEXT_LEFT_OFFSET - 8,
            textRenderer.fontHeight,
            ScreenTexts.EMPTY
        );
        advancementsSearchField.setDrawsBackground(false);
        advancementsSearchField.setEditableColor(Colors.WHITE);
        advancementsSearchField.setFocusUnlocked(false);
        addSelectableChild(advancementsSearchField);
        setInitialFocus(advancementsSearchField);
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
        if (screenState == AdvancementsScreenState.INFO_VISIBLE &&
            advancementsSearchField != null &&
            advancementsSearchField.mouseClicked(mouseX, mouseY, button)
        ) {
            cir.setReturnValue(true);
        }
        if (screenState != AdvancementsScreenState.WINDOW_VISIBLE) {
            cir.setReturnValue(false);
        }
        if (focusedPlacedAdvancement != null &&
            !isFocusedAdvancementClicked &&
            button == GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) {
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
        if (screenState != AdvancementsScreenState.WINDOW_VISIBLE) {
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
        if (horizontalAmount == 0 && verticalAmount != 0 && screenState == AdvancementsScreenState.INFO_VISIBLE) {
            currentAdvancementIndex -= (int) verticalAmount;
            currentAdvancementIndex = MathHelper.clamp(currentAdvancementIndex, 0, advancementsList.size() - 1);
            cir.setReturnValue(false);
        }
        if (screenState != AdvancementsScreenState.WINDOW_VISIBLE) {
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
        if (client == null) {
            return;
        }
        if (screenState == AdvancementsScreenState.OPENING_INFO) {
            tooltipTransitionProgress += (delta / 20.0f) / ANIMATION_DURATION_SECONDS;
            if (tooltipTransitionProgress >= 1.0f) {
                tooltipTransitionProgress = 1.0f;
                screenState = AdvancementsScreenState.INFO_VISIBLE;
                transitionAdvancementWidget = null;
            }
            if (transitionAdvancementWidget == null && focusedAdvancementWidget != null) {
                AdvancementTab focusedAdvancementTab = focusedAdvancementWidget.tab;
                transitionAdvancementWidget = new AdvancementWidget(
                    focusedAdvancementTab,
                    client,
                    focusedAdvancementWidget.advancement,
                    focusedAdvancementWidget.display
                );
                transitionAdvancementWidget.setProgress(focusedAdvancementWidget.progress);
                if (focusedAdvancementWidget instanceof AdvancementWidgetExtension advancementWidgetExtension) {
                    int tooltipWidth = advancementWidgetExtension.advancementsexplorer$getTooltipWidth();
                    int tooltipHeight = advancementWidgetExtension.advancementsexplorer$getTooltipHeight(true);
                    tooltipTransitionEndX = width / 4 - tooltipWidth / 2;
                    tooltipTransitionEndY = getCenterTooltipY(advancementWidgetExtension);
                    shouldHorizontallySwapTransitionTooltip =
                        advancementWidgetExtension.advancementsexplorer$isTooltipMirroredHorizontally();
                    shouldVerticallySwapTransitionTooltip =
                        advancementWidgetExtension.advancementsexplorer$isTooltipMirroredVertically();
                    tooltipTransitionStartX =
                        windowTreeX + MathHelper.floor(focusedAdvancementTab.originX) + focusedAdvancementWidget.x;
                    tooltipTransitionStartY =
                        windowTreeY + MathHelper.floor(focusedAdvancementTab.originY) + focusedAdvancementWidget.y;
                    if (shouldHorizontallySwapTransitionTooltip) {
                        tooltipTransitionStartX -= tooltipWidth;
                        tooltipTransitionStartX += Constants.ADVANCEMENT_FRAME_SIZE +
                            Constants.ADVANCEMENT_FRAME_OVERHANG * 2;
                    }
                    if (shouldVerticallySwapTransitionTooltip) {
                        tooltipTransitionStartY -= tooltipHeight;
                        tooltipTransitionStartY += Constants.ADVANCEMENT_FRAME_SIZE -
                            Constants.ADVANCEMENT_FRAME_OVERHANG * 2;
                    }
                }
                if (transitionAdvancementWidget instanceof AdvancementWidgetExtension advancementWidgetExtension) {
                    advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipHorizontally(false);
                    advancementWidgetExtension.advancementsexplorer$setForceMirrorTooltipVertically(false);
                    advancementWidgetExtension.advancementsexplorer$setX(tooltipTransitionStartX);
                    advancementWidgetExtension.advancementsexplorer$setY(tooltipTransitionStartY);
                }
                focusedAdvancementWidget = null;
            }
        }
        if (screenState != AdvancementsScreenState.WINDOW_VISIBLE) {
            AdvancementWidget centerWidget = getListAdvancementWidget(currentAdvancementIndex);
            if (centerWidget instanceof AdvancementWidgetExtension centerAdvancementWidgetExtension) {
                int listContainerWidth = width / 2;
                int listContainerHeight = height;
                int listContainerLeft = 0;
                int listContainerTop = 0;
                int listContainerRight = listContainerLeft + listContainerWidth;
                int listContainerBottom = listContainerTop + listContainerHeight;

                int headerWidth = listContainerWidth;
                int headerHeight = PAGE_OFFSET_Y;
                int headerLeft = listContainerLeft;
                int headerTop = listContainerTop;
                int headerRight = headerLeft + headerWidth;
                int headerBottom = headerTop + headerHeight;

                int footerWidth = listContainerWidth;
                int footerHeight = PAGE_OFFSET_X;
                int footerLeft = listContainerLeft;
                int footerTop = listContainerBottom - footerHeight;
                int footerRight = footerLeft + footerWidth;
                int footerBottom = footerTop + footerHeight;

                int listWidth = listContainerWidth;
                int listHeight = listContainerHeight - headerHeight - footerHeight;
                int listLeft = listContainerLeft;
                int listTop = headerBottom;
                int listRight = listLeft + listWidth;
                int listBottom = listTop + listHeight;
                int listCenterX = listContainerWidth / 2;
                int listCenterY = headerHeight + listHeight / 2;

                int listSpacing = 10;
                int listCenterExtraSpacing = 8;

                int centerTooltipWidth = centerAdvancementWidgetExtension.advancementsexplorer$getTooltipWidth();
                int centerTooltipHeight = centerAdvancementWidgetExtension.advancementsexplorer$getTooltipHeight(true);
                int centerTooltipX = listCenterX - centerTooltipWidth / 2;
                int centerTooltipY = getCenterTooltipY(centerAdvancementWidgetExtension);

                centerAdvancementWidgetExtension.advancementsexplorer$setX(centerTooltipX);
                centerAdvancementWidgetExtension.advancementsexplorer$setY(centerTooltipY);
                centerAdvancementWidgetExtension.advancementsexplorer$setCollapsed(false);

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 0);
                int prevDarkeningSectionBottom = centerTooltipY - listCenterExtraSpacing + Constants.ADVANCEMENT_FRAME_OVERHANG;
                int nextDarkeningSectionTop = centerTooltipY + centerTooltipHeight + listCenterExtraSpacing + Constants.ADVANCEMENT_FRAME_OVERHANG;
                int darkeningHeight = centerTooltipHeight + listCenterExtraSpacing * 2;
                renderDarkeningSection(
                    context,
                    listContainerLeft,
                    listContainerTop,
                    listContainerWidth,
                    prevDarkeningSectionBottom - listContainerTop
                );
                renderDarkeningSection(
                    context,
                    listContainerLeft,
                    nextDarkeningSectionTop,
                    listContainerWidth,
                    listContainerBottom - nextDarkeningSectionTop
                );
                context.drawVerticalLine(listContainerLeft + listContainerWidth, listContainerTop - 1, prevDarkeningSectionBottom - 1, 0xbf000000);
                context.drawVerticalLine(listContainerLeft + listContainerWidth + 1, listContainerTop - 1, prevDarkeningSectionBottom - 1, 0x33ffffff);
                context.drawHorizontalLine(listContainerLeft + listContainerWidth, listContainerLeft + listContainerWidth + 1, prevDarkeningSectionBottom - 1, 0x33ffffff);

                context.drawVerticalLine(listContainerWidth, nextDarkeningSectionTop, listContainerBottom, 0xbf000000);
                context.drawVerticalLine(listContainerWidth + 1, nextDarkeningSectionTop, listContainerBottom, 0x33ffffff);
                context.drawHorizontalLine(listContainerLeft + listContainerWidth, listContainerLeft + listContainerWidth + 1, nextDarkeningSectionTop, 0x33ffffff);

                context.getMatrices().pop();
                if (screenState == AdvancementsScreenState.INFO_VISIBLE) {
                    centerWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
                }

                int nextIndex = currentAdvancementIndex;
                float nextTooltipTop = centerTooltipY + centerTooltipHeight + listSpacing + listCenterExtraSpacing;
                while (nextTooltipTop <= listBottom) {
                    nextIndex++;
                    AdvancementWidget nextWidget = getListAdvancementWidget(nextIndex);
                    if (!(nextWidget instanceof AdvancementWidgetExtension nextAdvancementWidgetExtension)) {
                        break;
                    }
                    nextAdvancementWidgetExtension.advancementsexplorer$setCollapsed(true);
                    int nextTooltipWidth = nextAdvancementWidgetExtension.advancementsexplorer$getTooltipWidth();
                    int nextTooltipHeight = nextAdvancementWidgetExtension.advancementsexplorer$getTooltipHeight(false);
                    int nextTooltipCenterX = nextTooltipWidth / 2;
                    int nextTooltipX = listCenterX - nextTooltipCenterX;

                    nextAdvancementWidgetExtension.advancementsexplorer$setX(nextTooltipX);
                    nextAdvancementWidgetExtension.advancementsexplorer$setY(MathHelper.floor(nextTooltipTop));

                    nextWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
                    nextTooltipTop += nextTooltipHeight + listSpacing;
                }

                int prevIndex = currentAdvancementIndex;
                float prevTooltipBottom = centerTooltipY - listSpacing - listCenterExtraSpacing;
                while (prevTooltipBottom >= listTop) {
                    prevIndex--;
                    AdvancementWidget prevWidget = getListAdvancementWidget(prevIndex);
                    if (!(prevWidget instanceof AdvancementWidgetExtension prevAdvancementWidgetExtension)) {
                        break;
                    }
                    prevAdvancementWidgetExtension.advancementsexplorer$setCollapsed(true);
                    int prevTooltipWidth = prevAdvancementWidgetExtension.advancementsexplorer$getTooltipWidth();
                    int prevTooltipHeight = prevAdvancementWidgetExtension.advancementsexplorer$getTooltipHeight(false);
                    int prevTooltipCenterX = prevTooltipWidth / 2;
                    int prevTooltipX = listCenterX - prevTooltipCenterX;
                    float prevTooltipY = prevTooltipBottom - prevTooltipHeight;

                    prevAdvancementWidgetExtension.advancementsexplorer$setX(prevTooltipX);
                    prevAdvancementWidgetExtension.advancementsexplorer$setY(MathHelper.floor(prevTooltipY));

                    prevWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
                    prevTooltipBottom -= prevTooltipHeight + listSpacing;
                }

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 1000);
                int shadowHeight = 6;
                int windowTextureSize = 256;
                int textureRegionLeft = PAGE_OFFSET_X + shadowHeight;
                int textureRegionWidth = PAGE_WIDTH - textureRegionLeft;
                int textureRegionHeaderHeight = headerHeight + shadowHeight;
                int textureRegionHeaderTop = 0;
                int textureRegionFooterHeight = footerHeight + shadowHeight;
                int textureRegionFooterTop = WINDOW_HEIGHT - textureRegionFooterHeight;
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    WINDOW_TEXTURE,
                    headerLeft,
                    headerTop,
                    textureRegionLeft,
                    textureRegionHeaderTop,
                    headerWidth,
                    textureRegionHeaderHeight,
                    textureRegionWidth,
                    textureRegionHeaderHeight,
                    windowTextureSize,
                    windowTextureSize
                );

                context.drawText(
                    textRenderer,
                    advancementsSearchField.getText().isEmpty() ? ADVANCEMENTS_TEXT : SEARCH_TITLE,
                    headerLeft + 6,
                    headerTop + 6,
                    4210752,
                    false
                );

                int advancementsSearchFieldX = headerRight - SEARCH_FIELD_WIDTH - 3;
                int advancementsSearchFieldY = (headerHeight - SEARCH_FIELD_HEIGHT) / 2 + 1;
                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    ItemGroups.ITEM_SEARCH_TAB_TEXTURE_ID,
                    advancementsSearchFieldX,
                    advancementsSearchFieldY,
                    80,
                    4,
                    SEARCH_FIELD_WIDTH,
                    SEARCH_FIELD_HEIGHT,
                    256,
                    256
                );
                advancementsSearchField.setX(advancementsSearchFieldX + SEARCH_FIELD_TEXT_LEFT_OFFSET);
                advancementsSearchField.setY(advancementsSearchFieldY + SEARCH_FIELD_TEXT_LEFT_OFFSET);
                advancementsSearchField.render(context, mouseX, mouseY, delta);

                context.drawTexture(
                    RenderLayer::getGuiTextured,
                    WINDOW_TEXTURE,
                    footerLeft,
                    footerTop - shadowHeight,
                    textureRegionLeft,
                    textureRegionFooterTop,
                    footerWidth,
                    textureRegionFooterHeight,
                    textureRegionWidth,
                    textureRegionFooterHeight,
                    windowTextureSize,
                    windowTextureSize
                );
                context.getMatrices().pop();
            }
            if (screenState == AdvancementsScreenState.OPENING_INFO &&
                transitionAdvancementWidget instanceof AdvancementWidgetExtension advancementWidgetExtension
            ) {
                float progress = tooltipTransitionProgress;
                float easedProgress = (float) Math.pow(progress, 2.2d);
                float inverse = 1.0f - easedProgress;
                float startX = tooltipTransitionStartX;
                float middleX = width / 2f;
                float endX = tooltipTransitionEndX;
                float newX = inverse * inverse * startX +
                    2.0f * inverse * easedProgress * middleX +
                    easedProgress * easedProgress * endX;
                float startY = tooltipTransitionStartY;
                float middleY = height / 2f;
                float endY = tooltipTransitionEndY;
                float newY = inverse * inverse * startY +
                    2.0f * inverse * easedProgress * middleY +
                    easedProgress * easedProgress * endY;
                advancementWidgetExtension.advancementsexplorer$setX(MathHelper.floor(newX));
                advancementWidgetExtension.advancementsexplorer$setY(MathHelper.floor(newY));

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
            if (screenState == AdvancementsScreenState.INFO_VISIBLE) {
                screenState = AdvancementsScreenState.WINDOW_VISIBLE;
                cir.setReturnValue(true);
            } else if (screenState == AdvancementsScreenState.OPENING_INFO) {
                cir.setReturnValue(true);
            }
        } else if (screenState == AdvancementsScreenState.OPENING_INFO) {
            cir.setReturnValue(true);
        }
        if (screenState == AdvancementsScreenState.INFO_VISIBLE && advancementsSearchField != null) {
            String oldText = advancementsSearchField.getText();
            if (advancementsSearchField.keyPressed(keyCode, scanCode, modifiers)) {
                if (!Objects.equals(oldText, advancementsSearchField.getText())) {
                    searchAdvancements();
                }
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
