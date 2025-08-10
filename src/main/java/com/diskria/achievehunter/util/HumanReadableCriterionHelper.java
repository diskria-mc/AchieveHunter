package com.diskria.achievehunter.util;

import com.diskria.achievehunter.api.AddonsManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

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
public class HumanReadableCriterionHelper {

    private static final Map<String, Criterion> CACHE = new HashMap<>();

    public static void clearCache() {
        CACHE.clear();
    }

    public static Criterion getHumanReadableCriterion(
            @NotNull Identifier advancementId,
            @NotNull String realCriterionName
    ) {
        if (realCriterionName.trim().isEmpty()) {
            return new Criterion(null, null);
        }

        return CACHE.computeIfAbsent(
                advancementId + "@" + realCriterionName,
                k -> initCriterion(advancementId, realCriterionName)
        );
    }

    private static @NotNull Criterion initCriterion(
            @NotNull Identifier advancementId,
            @NotNull String realCriterionName
    ) {
        String translation = AddonsManager.findCriterionTranslation(advancementId, realCriterionName);
        CriterionIcon icon = AddonsManager.findCriterionIcon(advancementId, realCriterionName);
        if (translation != null && icon != null) {
            return new Criterion(translation, icon);
        }
        if (translation == null) {
            String resourcePackTranslation = Utils.getTranslationOrNull(
                    "advancement."
                            + advancementId.toString().replace(":", ".").replace("/", ".")
                            + "."
                            + realCriterionName.replace(":", ".").replace("/", ".")
            );
            if (resourcePackTranslation != null) {
                translation = resourcePackTranslation;
            }
        }

        List<String> namespaces = new ArrayList<>();
        String advancementNamespace = advancementId.getNamespace();
        namespaces.add(advancementNamespace);
        if (!advancementNamespace.equals(Identifier.DEFAULT_NAMESPACE)) {
            namespaces.add(Identifier.DEFAULT_NAMESPACE);
        }
        for (String namespace : namespaces) {
            String namespacePrefix = namespace + ":";
            String criterionName = realCriterionName.startsWith(namespacePrefix)
                    ? realCriterionName.replace(namespacePrefix, "")
                    : realCriterionName;
            Identifier id = Identifier.tryParse(namespace, criterionName);
            if (id == null) {
                continue;
            }

            String autoDetectedTranslation = null;
            CriterionType criterionType = null;
            CriterionType forceCriterionType = AddonsManager.findForceCriterionType(advancementId, realCriterionName);
            if (forceCriterionType != null) {
                autoDetectedTranslation = Utils.getTranslationOrNull(
                        CriterionLocaleKeyResolver.resolve(forceCriterionType, id)
                );
                criterionType = forceCriterionType;
            } else {
                for (CriterionType type : CriterionType.values()) {
                    autoDetectedTranslation = Utils.getTranslationOrNull(
                            CriterionLocaleKeyResolver.resolve(type, id)
                    );
                    if (autoDetectedTranslation != null) {
                        criterionType = type;
                        break;
                    }
                }
            }
            if (autoDetectedTranslation != null) {
                if (icon == null) {
                    icon = CriterionIconResolver.resolveIcon(criterionType, id);
                }
                if (translation == null) {
                    translation = autoDetectedTranslation;
                }
                break;
            }
        }
        String criterionName;
        if (translation != null) {
            criterionName = translation;
        } else {
            criterionName = realCriterionName
                    .replace(":", " ")
                    .replace("/", " ")
                    .replace("_", " ")
                    .trim();
            if (!criterionName.isEmpty()) {
                criterionName = Utils.capitalizeFirst(criterionName);
            } else {
                criterionName = null;
            }
        }
        return new Criterion(criterionName, icon);
    }
}
