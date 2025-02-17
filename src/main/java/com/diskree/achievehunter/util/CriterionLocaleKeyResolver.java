package com.diskree.achievehunter.util;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class CriterionLocaleKeyResolver {

    public static @NotNull String resolve(@NotNull CriterionType type, @NotNull Identifier id) {
        return type
            .getLocaleFormat()
            .replace("%namespace%", id.getNamespace())
            .replace("%criterion%", id.getPath());
    }
}
