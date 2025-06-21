package com.diskree.achievehunter.gui;

import com.diskree.achievehunter.util.CriterionIcon;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AdvancementCriterionItem(
    @NotNull String rawName,
    @NotNull Text displayedText,
    @Nullable CriterionIcon icon
) {
}
