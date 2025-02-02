package com.diskree.achievehunter.injection.extension;

public interface AdvancementWidgetExtension {
    boolean achievehunter$isTooltipMirroredHorizontally();

    boolean achievehunter$isTooltipMirroredVertically();

    void achievehunter$setForceMirrorTooltipHorizontally(Boolean forceMirrorTooltipHorizontally);

    void achievehunter$setForceMirrorTooltipVertically(Boolean forceMirrorTooltipVertically);

    int achievehunter$getTooltipWidth();

    int achievehunter$getTooltipHeight(boolean withDescription);

    void achievehunter$setX(int x);

    void achievehunter$setY(int y);

    void achievehunter$setHorizontallySwapAnimationProgress(float horizontallySwapAnimationProgress);

    void achievehunter$setVerticallySwapAnimationProgress(float verticallySwapAnimationProgress);

    void achievehunter$setCollapsed(boolean isCollapsed);
}
