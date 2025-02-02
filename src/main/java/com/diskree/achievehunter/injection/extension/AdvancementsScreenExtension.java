package com.diskree.achievehunter.injection.extension;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import org.jetbrains.annotations.Nullable;

public interface AdvancementsScreenExtension {
    void achievehunter$setFocusedAdvancement(@Nullable AdvancementWidget advancementWidget);

    void achievehunter$onMouseReleased(double mouseX, double mouseY, int button);

    void achievehunter$tick();

    boolean achievehunter$charTyped(char chr, int modifiers);

    void achievehunter$resize(MinecraftClient client, int width, int height);
}
