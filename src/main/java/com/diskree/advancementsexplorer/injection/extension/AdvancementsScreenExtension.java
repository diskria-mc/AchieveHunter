package com.diskree.advancementsexplorer.injection.extension;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.jetbrains.annotations.Nullable;

public interface AdvancementsScreenExtension {
    void advancementsexplorer$setFocusedAdvancement(@Nullable AdvancementWidget advancementWidget);

    void advancementsexplorer$onMouseReleased(double mouseX, double mouseY, int button);

    void advancementsexplorer$tick();

    boolean advancementsexplorer$charTyped(char chr, int modifiers);

    void advancementsexplorer$resize(MinecraftClient client, int width, int height);
}
