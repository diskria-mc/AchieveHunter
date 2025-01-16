package com.diskree.advancementsexplorer.injection.extension;

public interface AdvancementWidgetExtension {
    boolean advancementsexplorer$isTooltipMirroredHorizontally();

    boolean advancementsexplorer$isTooltipMirroredVertically();

    void advancementsexplorer$setForceMirrorTooltipHorizontally(Boolean forceMirrorTooltipHorizontally);

    void advancementsexplorer$setForceMirrorTooltipVertically(Boolean forceMirrorTooltipVertically);

    int advancementsexplorer$getTooltipHeight();

    int advancementsexplorer$getTooltipX();

    int advancementsexplorer$getTooltipY();

    void advancementsexplorer$setX(int x);

    void advancementsexplorer$setY(int y);

    void advancementsexplorer$setHorizontallySwapAnimationProgress(float horizontallySwapAnimationProgress);

    void advancementsexplorer$setVerticallySwapAnimationProgress(float verticallySwapAnimationProgress);
}
