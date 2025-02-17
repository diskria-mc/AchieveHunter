package com.diskree.achievehunter.gui;

import com.diskree.achievehunter.util.CriterionIcon;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record AdvancementCriterionItem(
    @NotNull String rawName,
    @NotNull Text displayedText,
    @NotNull CriterionIcon icon
) {
}
