package com.diskria.achievehunter.util;

import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class Utils {

    public static @NotNull String capitalizeFirst(@NotNull String string) {
        if (string.isEmpty()) {
            return string;
        }
        return string.substring(0, 1).toUpperCase(Locale.ROOT)
                + string.substring(1).toLowerCase(Locale.ROOT);
    }

    public static @Nullable String getTranslationOrNull(@Nullable String key) {
        if (key == null) {
            return null;
        }
        String translated = Text.translatable(key).getString();
        if (!translated.equals(key)) {
            return translated;
        }
        Language language = Language.getInstance();
        if (language != null && language.hasTranslation(key)) {
            return language.get(key);
        }
        return null;
    }
}
