package com.diskree.achievehunter.util;

import com.diskree.achievehunter.api.AddonsManager;
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
            return new Criterion(null, CriterionIcon.NO_ICON);
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
        CriterionIcon addonCriterionIcon = AddonsManager.findCriterionIcon(advancementId, realCriterionName);
        String addonTranslationKey = AddonsManager.findCriterionTranslation(advancementId, realCriterionName);
        if (addonTranslationKey != null) {
            return new Criterion(addonTranslationKey, addonCriterionIcon);
        }
        String resourcePackTranslation = Utils.getTranslationOrNull(
            "advancement."
                + advancementId.toString().replace(":", ".").replace("/", ".")
                + "."
                + realCriterionName.replace(":", ".").replace("/", ".")
        );
        if (resourcePackTranslation != null) {
            return new Criterion(resourcePackTranslation, addonCriterionIcon);
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

            for (CriterionType criterionType : CriterionType.values()) {
                String criterionTranslation = Utils.getTranslationOrNull(
                    CriterionLocaleKeyResolver.resolve(criterionType, id)
                );
                if (criterionTranslation != null) {
                    CriterionIcon icon = CriterionIconResolver.resolveIcon(criterionType, id);
                    return new Criterion(criterionTranslation, icon != null ? icon : addonCriterionIcon);
                }
            }
        }

        realCriterionName = realCriterionName
            .replace(":", " ")
            .replace("/", " ")
            .replace("_", " ")
            .trim();
        return new Criterion(
            !realCriterionName.isEmpty() ? Utils.capitalizeFirst(realCriterionName) : null,
            addonCriterionIcon
        );
    }
}
