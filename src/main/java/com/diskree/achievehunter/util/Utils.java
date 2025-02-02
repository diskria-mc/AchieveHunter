package com.diskree.achievehunter.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class Utils {

    public static @NotNull String capitalizeFirst(@NotNull String string) {
        if (string.isEmpty()) {
            return string;
        }
        return string.substring(0, 1).toUpperCase(Locale.ROOT)
            + string.substring(1).toLowerCase(Locale.ROOT);
    }
}
