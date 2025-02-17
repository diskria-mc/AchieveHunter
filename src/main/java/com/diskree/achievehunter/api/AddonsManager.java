package com.diskree.achievehunter.api;

import com.diskree.achievehunter.BuildConfig;
import com.diskree.achievehunter.util.CriterionIcon;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AddonsManager {

    private static final List<AchieveHunterApi> ADDONS = new ArrayList<>();

    public static void init() {
        ADDONS.addAll(FabricLoader.getInstance().getEntrypoints(BuildConfig.MOD_ID, AchieveHunterApi.class));
    }

    public static @Nullable String findCriterionTranslation(Identifier advancementId, String criterionName) {
        for (AchieveHunterApi addon : ADDONS) {
            String key = addon.getCriterionTranslation(advancementId, criterionName);
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    public static @NotNull CriterionIcon findCriterionIcon(Identifier advancementId, String criterionName) {
        for (AchieveHunterApi addon : ADDONS) {
            CriterionIcon icon = addon.getCriterionIcon(advancementId, criterionName);
            if (icon != null) {
                return icon;
            }
        }
        return CriterionIcon.NO_ICON;
    }
}
