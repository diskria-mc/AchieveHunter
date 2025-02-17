package com.diskree.achievehunter.api;

import com.diskree.achievehunter.util.CriterionIcon;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface AchieveHunterApi {

    /**
     * Gets the translation for a criterion.
     *
     * @param advancementId The advancement identifier
     * @param criterionName The technical criterion name
     * @return The translation or {@code null} if not provided
     */
    @Nullable String getCriterionTranslation(Identifier advancementId, String criterionName);

    /**
     * Gets the icon for a criterion.
     *
     * @param advancementId The advancement identifier
     * @param criterionName The technical criterion name
     * @return The {@link CriterionIcon}, or {@code null} if not provided
     */
    @Nullable CriterionIcon getCriterionIcon(Identifier advancementId, String criterionName);
}
