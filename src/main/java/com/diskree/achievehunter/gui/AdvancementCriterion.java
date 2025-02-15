package com.diskree.achievehunter.gui;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AdvancementCriterion(@NotNull String rawName, @NotNull Text name, @Nullable Identifier iconTexture) {
}
