package com.diskree.advancementsexplorer.injection.extension;

import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface AdvancementsScreenExtension {
    void advancementsexplorer$setFocusedAdvancement(
        @Nullable Identifier advancementId,
        @Nullable AdvancementWidget advancementWidget
    );

    void advancementsexplorer$onMouseReleased(double mouseX, double mouseY, int button);

    void advancementsexplorer$tick();

    boolean advancementsexplorer$charTyped(char chr, int modifiers);
}
