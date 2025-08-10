package com.diskria.achievehunter.injection.extension;

import com.diskria.achievehunter.util.HighlightType;
import com.diskria.achievehunter.util.SearchByType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface AdvancementsScreenExtension {
    void achievehunter$setFocusedAdvancement(@Nullable AdvancementWidget advancementWidget);

    void achievehunter$onMouseReleased(double mouseX, double mouseY, int button);

    void achievehunter$tick();

    boolean achievehunter$charTyped(char chr, int modifiers);

    int achievehunter$getWindowWidth();

    int achievehunter$getWindowHeight();

    int achievehunter$getTreeWidth();

    int achievehunter$getTreeHeight();

    int achievehunter$getWindowHorizontalMargin();

    int achievehunter$getWindowVerticalMargin();

    void achievehunter$resize(MinecraftClient client, int width, int height);

    boolean achievehunter$isSearchActive();

    Identifier achievehunter$getHighlightedAdvancementId();

    HighlightType achievehunter$getHighlightType();

    boolean achievehunter$isHighlightAtInvisibleState();

    void achievehunter$stopHighlight();

    void achievehunter$searchAdvancements(
            String query,
            SearchByType searchByType,
            boolean autoHighlightSingle,
            HighlightType highlightType
    );

    void achievehunter$highlightAdvancement(Identifier advancementId, HighlightType highlightType);
}
