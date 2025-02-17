package com.diskree.achievehunter.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Criterion(@Nullable String criterionName, @NotNull CriterionIcon icon) {
}
