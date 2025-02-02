package com.diskree.achievehunter.util;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Inspired by code from <BOs-Advancements-Tracker> by Markus Bordihn (c) 2021,
 * licensed under the MIT License.
 *
 * See: https://github.com/MarkusBordihn/BOs-Advancements-Tracker
 */
public class HumanReadableCriterionNameHelper {

    private static final List<String> TRANSLATION_FORMATS = List.of(
        "item.%namespace%.%criterion%",
        "block.%namespace%.%criterion%",
        "entity.%namespace%.%criterion%",
        "entity.%namespace%.villager.%criterion%",
        "enchantment.%namespace%.%criterion%",
        "effect.%namespace%.%criterion%",
        "biome.%namespace%.%criterion%",
        "structure.%namespace%.%criterion%",
        "color.%namespace%.%criterion%",
        "instrument.%namespace%.%criterion%",
        "jukebox_song.%namespace%.%criterion%",
        "stat.%namespace%.%criterion%",
        "trim_material.%namespace%.%criterion%",
        "trim_pattern.%namespace%.%criterion%",
        "painting.%namespace%.%criterion%.title"
    );
    private static final Map<String, String> NAME_CACHE = new HashMap<>();

    public static String getHumanReadableCriterionName(
        @NotNull Identifier advancementId,
        @NotNull String technicallyCriterionName
    ) {
        if (technicallyCriterionName.trim().isEmpty()) {
            return "";
        }

        return NAME_CACHE.computeIfAbsent(
            advancementId + "@" + technicallyCriterionName,
            k -> computeName(advancementId, technicallyCriterionName)
        );
    }

    public static void clearCache() {
        NAME_CACHE.clear();
    }

    private static @NotNull String computeName(@NotNull Identifier advancementId, String technicallyCriterionName) {
        List<String> namespaces = new ArrayList<>();
        String advancementNamespace = advancementId.getNamespace();
        namespaces.add(advancementNamespace);
        if (!advancementNamespace.equals(Identifier.DEFAULT_NAMESPACE)) {
            namespaces.add(Identifier.DEFAULT_NAMESPACE);
        }
        String customKey =
            "advancement."
                + advancementId.toString().replace(":", ".").replace("/", ".")
                + "."
                + technicallyCriterionName.replace(":", ".").replace("/", ".");
        String customTranslation = getTranslationOrNull(customKey);
        if (customTranslation != null) {
            return customTranslation;
        }

        for (String namespace : namespaces) {
            String criterionName = technicallyCriterionName.startsWith(namespace + ":")
                ? technicallyCriterionName.replace(namespace + ":", "")
                : technicallyCriterionName;

            for (String format : TRANSLATION_FORMATS) {
                String translation = getTranslationOrNull(format
                    .replace("%namespace%", namespace)
                    .replace("%criterion%", criterionName));
                if (translation != null) {
                    return translation;
                }
            }
        }

        technicallyCriterionName = technicallyCriterionName
            .replace(":", " ")
            .replace("/", " ")
            .replace("_", " ")
            .trim();
        if (technicallyCriterionName.isEmpty()) {
            return "";
        }
        return Utils.capitalizeFirst(technicallyCriterionName);
    }

    private static @Nullable String getTranslationOrNull(String key) {
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
